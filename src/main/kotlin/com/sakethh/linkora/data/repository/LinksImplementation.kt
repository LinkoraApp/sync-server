package com.sakethh.linkora.data.repository

import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.Route
import com.sakethh.linkora.domain.dto.IDBasedDTO
import com.sakethh.linkora.domain.dto.NewItemResponseDTO
import com.sakethh.linkora.domain.dto.TimeStampBasedResponse
import com.sakethh.linkora.domain.dto.link.*
import com.sakethh.linkora.domain.model.WebSocketEvent
import com.sakethh.linkora.domain.repository.LinksRepository
import com.sakethh.linkora.domain.tables.LinksTable
import com.sakethh.linkora.domain.tables.LinksTable.lastModified
import com.sakethh.linkora.domain.tables.TombstoneTable
import com.sakethh.linkora.domain.tables.helper.TombStoneHelper
import com.sakethh.linkora.utils.checkForLWWConflictAndThrow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

class LinksImplementation : LinksRepository {
    override suspend fun createANewLink(addLinkDTO: AddLinkDTO): Result<NewItemResponseDTO> {
        return try {
            val eventTimestamp = Instant.now().epochSecond
            transaction {
                if (addLinkDTO.linkType == LinkType.HISTORY_LINK) {
                    LinksTable.url.eq(addLinkDTO.url).and(LinksTable.linkType.eq(LinkType.HISTORY_LINK.name))
                        .let { condition ->
                            LinksTable.selectAll().where {
                                condition
                            }.toList().let { resultRows ->
                                TombstoneTable.batchInsert(resultRows) {
                                    it[TombstoneTable.deletedAt] = eventTimestamp
                                    it[TombstoneTable.operation] = Route.Link.DELETE_A_LINK.name
                                    it[TombstoneTable.payload] = Json.encodeToString(
                                        IDBasedDTO(
                                            id = it[LinksTable.id].value,
                                            correlation = addLinkDTO.correlation,
                                            eventTimestamp = eventTimestamp
                                        )
                                    )
                                }
                            }
                            LinksTable.deleteWhere {
                                condition
                            }
                    }
                }
                LinksTable.insertAndGetId { link ->
                    link[lastModified] = eventTimestamp
                    link[linkType] = addLinkDTO.linkType.name
                    link[linkTitle] = addLinkDTO.title
                    link[url] = addLinkDTO.url
                    link[baseURL] = addLinkDTO.baseURL
                    link[imgURL] = addLinkDTO.imgURL
                    link[note] = addLinkDTO.note
                    link[idOfLinkedFolder] = addLinkDTO.idOfLinkedFolder
                    link[userAgent] = addLinkDTO.userAgent
                    link[mediaType] = addLinkDTO.mediaType.name
                    link[markedAsImportant] = addLinkDTO.markedAsImportant
                }
            }.value.let { idOfNewlyAddedLink ->
                Result.Success(
                    response = NewItemResponseDTO(
                        timeStampBasedResponse = TimeStampBasedResponse(
                            message = "Link created successfully for ${addLinkDTO.linkType.name} with id = ${idOfNewlyAddedLink}.",
                            eventTimestamp = eventTimestamp
                        ),
                        id = idOfNewlyAddedLink,
                        correlation = addLinkDTO.correlation
                    ), webSocketEvent = WebSocketEvent(
                        operation = Route.Link.CREATE_A_NEW_LINK.name, payload = Json.encodeToJsonElement(
                            LinkDTO(
                                id = idOfNewlyAddedLink,
                                linkType = addLinkDTO.linkType,
                                title = addLinkDTO.title,
                                url = addLinkDTO.url,
                                baseURL = addLinkDTO.baseURL,
                                imgURL = addLinkDTO.imgURL,
                                note = addLinkDTO.note,
                                idOfLinkedFolder = addLinkDTO.idOfLinkedFolder,
                                userAgent = addLinkDTO.userAgent,
                                markedAsImportant = addLinkDTO.markedAsImportant,
                                mediaType = addLinkDTO.mediaType,
                                correlation = addLinkDTO.correlation,
                                eventTimestamp
                            )
                        )
                    )
                )
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }


    override suspend fun deleteALink(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse> {
        return try {
            val eventTimestamp = Instant.now().epochSecond
            transaction {
                TombStoneHelper.insert(
                    payload = Json.encodeToString(idBasedDTO.copy(eventTimestamp = eventTimestamp)),
                    operation = Route.Link.DELETE_A_LINK.name,
                    deletedAt = eventTimestamp
                )
                LinksTable.deleteWhere {
                    id.eq(idBasedDTO.id)
                }
            }
            Result.Success(
                response = TimeStampBasedResponse(
                    eventTimestamp = eventTimestamp,
                    message = "Link deleted successfully."
                ), webSocketEvent = WebSocketEvent(
                    operation = Route.Link.DELETE_A_LINK.name,
                    payload = Json.encodeToJsonElement(idBasedDTO.copy(eventTimestamp = eventTimestamp)),
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun updateLinkedFolderIdOfALink(updateLinkedFolderIDDto: UpdateLinkedFolderIDDto): Result<TimeStampBasedResponse> {
        return try {
            LinksTable.checkForLWWConflictAndThrow(
                id = updateLinkedFolderIDDto.linkId,
                timeStamp = updateLinkedFolderIDDto.eventTimestamp,
                lastModifiedColumn = lastModified
            )
            val eventTimestamp = Instant.now().epochSecond
            transaction {
                LinksTable.update(where = {
                    LinksTable.id.eq(updateLinkedFolderIDDto.linkId) and LinksTable.linkType.eq(
                        updateLinkedFolderIDDto.linkType.name
                    )
                }) {
                    it[lastModified] = eventTimestamp
                    it[idOfLinkedFolder] = updateLinkedFolderIDDto.linkId
                }
            }
            Result.Success(
                response = TimeStampBasedResponse(
                    eventTimestamp = eventTimestamp,
                    message = "idOfLinkedFolder Updated Successfully."
                ), webSocketEvent = WebSocketEvent(
                    operation = Route.Link.UPDATE_LINKED_FOLDER_ID.name,
                    payload = Json.encodeToJsonElement(updateLinkedFolderIDDto.copy(eventTimestamp = eventTimestamp)),
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun updateTitleOfTheLink(updateTitleOfTheLinkDTO: UpdateTitleOfTheLinkDTO): Result<TimeStampBasedResponse> {
        return try {
            LinksTable.checkForLWWConflictAndThrow(
                id = updateTitleOfTheLinkDTO.linkId,
                timeStamp = updateTitleOfTheLinkDTO.eventTimestamp,
                lastModifiedColumn = lastModified
            )
            val eventTimestamp = Instant.now().epochSecond
            transaction {
                LinksTable.update(where = {
                    LinksTable.id.eq(updateTitleOfTheLinkDTO.linkId)
                }) {
                    it[lastModified] = eventTimestamp
                    it[linkTitle] = updateTitleOfTheLinkDTO.newTitleOfTheLink
                }

            }
            Result.Success(
                response = TimeStampBasedResponse(
                    message = "Title was updated successfully.",
                    eventTimestamp = eventTimestamp
                ), webSocketEvent = WebSocketEvent(
                    operation = Route.Link.UPDATE_LINK_TITLE.name,
                    payload = Json.encodeToJsonElement(updateTitleOfTheLinkDTO.copy(eventTimestamp = eventTimestamp)),
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun updateNote(updateNoteOfALinkDTO: UpdateNoteOfALinkDTO): Result<TimeStampBasedResponse> {
        return try {
            LinksTable.checkForLWWConflictAndThrow(
                id = updateNoteOfALinkDTO.linkId,
                timeStamp = updateNoteOfALinkDTO.eventTimestamp,
                lastModifiedColumn = lastModified
            )
            val eventTimestamp = Instant.now().epochSecond
            transaction {
                LinksTable.update(where = {
                    LinksTable.id.eq(updateNoteOfALinkDTO.linkId)
                }) {
                    it[lastModified] = eventTimestamp
                    it[note] = updateNoteOfALinkDTO.newNote
                }
            }
            Result.Success(
                response = TimeStampBasedResponse(
                    message = "Note was updated successfully.",
                    eventTimestamp = eventTimestamp
                ), webSocketEvent = WebSocketEvent(
                    operation = Route.Link.UPDATE_LINK_NOTE.name,
                    payload = Json.encodeToJsonElement(updateNoteOfALinkDTO.copy(eventTimestamp = eventTimestamp)),
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun updateUserAgent(updateLinkUserAgentDTO: UpdateLinkUserAgentDTO): Result<TimeStampBasedResponse> {
        return try {
            val eventTimestamp = Instant.now().epochSecond
            transaction {
                LinksTable.update(where = {
                    LinksTable.id.eq(updateLinkUserAgentDTO.linkId) and LinksTable.linkType.eq(
                        updateLinkUserAgentDTO.linkType.name
                    )
                }) {
                    it[lastModified] = eventTimestamp
                    it[this.userAgent] = userAgent
                }
            }
            Result.Success(
                response = TimeStampBasedResponse(
                    message = "User agent was updated successfully.",
                    eventTimestamp = eventTimestamp
                ), webSocketEvent = WebSocketEvent(
                    operation = Route.Link.UPDATE_USER_AGENT.name,
                    payload = Json.encodeToJsonElement(updateLinkUserAgentDTO.copy(eventTimestamp = eventTimestamp)),
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun archiveALink(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse> {
        return try {
            LinksTable.checkForLWWConflictAndThrow(
                id = idBasedDTO.id,
                timeStamp = idBasedDTO.eventTimestamp,
                lastModifiedColumn = lastModified
            )
            val eventTimestamp = Instant.now().epochSecond
            transaction {
                LinksTable.update(where = {
                    LinksTable.id.eq(idBasedDTO.id)
                }) {
                    it[lastModified] = eventTimestamp
                    it[linkType] = LinkType.ARCHIVE_LINK.name
                }
            }
            Result.Success(
                response = TimeStampBasedResponse(
                    message = "Archived link with id : ${idBasedDTO.id} successfully",
                    eventTimestamp = eventTimestamp
                ), webSocketEvent = WebSocketEvent(
                    operation = Route.Link.ARCHIVE_LINK.name,
                    payload = Json.encodeToJsonElement(idBasedDTO.copy(eventTimestamp = eventTimestamp))
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun unArchiveALink(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse> {
        return try {
            LinksTable.checkForLWWConflictAndThrow(
                id = idBasedDTO.id,
                timeStamp = idBasedDTO.eventTimestamp,
                lastModifiedColumn = lastModified
            )
            val eventTimestamp = Instant.now().epochSecond
            transaction {
                LinksTable.update(where = {
                    LinksTable.id.eq(idBasedDTO.id)
                }) {
                    it[lastModified] = eventTimestamp
                    it[linkType] = LinkType.SAVED_LINK.name
                }
            }
            Result.Success(
                response = TimeStampBasedResponse(
                    eventTimestamp = eventTimestamp,
                    message = "Unarchived link with id : ${idBasedDTO.id} successfully as ${LinkType.SAVED_LINK.name}"
                ),
                webSocketEvent = WebSocketEvent(
                    operation = Route.Link.UNARCHIVE_LINK.name,
                    payload = Json.encodeToJsonElement(idBasedDTO.copy(eventTimestamp = eventTimestamp))
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun markALinkAsImp(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse> {
        return try {
            LinksTable.checkForLWWConflictAndThrow(
                id = idBasedDTO.id,
                timeStamp = idBasedDTO.eventTimestamp,
                lastModifiedColumn = lastModified
            )
            val eventTimestamp = Instant.now().epochSecond
            transaction {
                LinksTable.update(where = {
                    LinksTable.id.eq(idBasedDTO.id)
                }) {
                    it[lastModified] = eventTimestamp
                    it[markedAsImportant] = true
                }
            }
            Result.Success(
                response = TimeStampBasedResponse(
                    message = "Marked link with id : ${idBasedDTO.id} as Important.",
                    eventTimestamp = eventTimestamp
                ), webSocketEvent = WebSocketEvent(
                    operation = Route.Link.MARK_AS_IMP.name,
                    payload = Json.encodeToJsonElement(idBasedDTO.copy(eventTimestamp = eventTimestamp))
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun markALinkAsNonImp(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse> {
        return try {
            LinksTable.checkForLWWConflictAndThrow(
                id = idBasedDTO.id,
                timeStamp = idBasedDTO.eventTimestamp,
                lastModifiedColumn = lastModified
            )
            val eventTimestamp = Instant.now().epochSecond
            transaction {
                LinksTable.update(where = {
                    LinksTable.id.eq(idBasedDTO.id)
                }) {
                    it[lastModified] = eventTimestamp
                    it[markedAsImportant] = false
                }
            }
            Result.Success(
                response = TimeStampBasedResponse(
                    message = "Marked link with id : ${idBasedDTO.id} as Non-Important.",
                    eventTimestamp = eventTimestamp
                ), webSocketEvent = WebSocketEvent(
                    operation = Route.Link.UNMARK_AS_IMP.name,
                    payload = Json.encodeToJsonElement(idBasedDTO.copy(eventTimestamp = eventTimestamp))
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun updateLink(linkDTO: LinkDTO): Result<TimeStampBasedResponse> {
        return try {
            LinksTable.checkForLWWConflictAndThrow(
                id = linkDTO.id,
                timeStamp = linkDTO.eventTimestamp,
                lastModifiedColumn = lastModified
            )
            val eventTimestamp = Instant.now().epochSecond
            transaction {
                LinksTable.update(where = {
                    LinksTable.id.eq(linkDTO.id)
                }) {
                    it[lastModified] = eventTimestamp
                    it[linkType] = linkDTO.linkType.name
                    it[linkTitle] = linkDTO.title
                    it[url] = linkDTO.url
                    it[baseURL] = linkDTO.baseURL
                    it[imgURL] = linkDTO.imgURL
                    it[note] = linkDTO.note
                    it[idOfLinkedFolder] = linkDTO.idOfLinkedFolder
                    it[userAgent] = linkDTO.userAgent
                    it[mediaType] = linkDTO.mediaType.name
                    it[markedAsImportant] = linkDTO.markedAsImportant
                }
            }
            Result.Success(
                response = TimeStampBasedResponse(
                    eventTimestamp = eventTimestamp,
                    message = "Updated the link (id : ${linkDTO.id}) successfully."
                ),
                webSocketEvent = WebSocketEvent(
                    operation = Route.Link.UPDATE_LINK.name,
                    payload = Json.encodeToJsonElement(linkDTO.copy(eventTimestamp = eventTimestamp))
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun deleteDuplicateLinks(deleteDuplicateLinksDTO: DeleteDuplicateLinksDTO): Result<TimeStampBasedResponse> {
        return try {
            val eventTimestamp = Instant.now().epochSecond
            transaction {
                LinksTable.deleteWhere {
                    LinksTable.id.inList(deleteDuplicateLinksDTO.linkIds)
                }
                TombStoneHelper.insert(
                    payload = Json.encodeToString(deleteDuplicateLinksDTO),
                    operation = Route.Link.DELETE_DUPLICATE_LINKS.name,
                    deletedAt = eventTimestamp
                )
            }
            Result.Success(
                response = TimeStampBasedResponse(
                    eventTimestamp = eventTimestamp, message = "Deleted Duplicate links."
                ), webSocketEvent = WebSocketEvent(
                    operation = Route.Link.DELETE_DUPLICATE_LINKS.name,
                    payload = Json.encodeToJsonElement(deleteDuplicateLinksDTO.copy(eventTimestamp = eventTimestamp))
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
}