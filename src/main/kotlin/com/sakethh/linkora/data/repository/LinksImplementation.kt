package com.sakethh.linkora.data.repository

import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.dto.IDBasedDTO
import com.sakethh.linkora.domain.dto.NewItemResponseDTO
import com.sakethh.linkora.domain.dto.link.*
import com.sakethh.linkora.domain.handler.LinksTombstoneHandler.insert
import com.sakethh.linkora.domain.model.WebSocketEvent
import com.sakethh.linkora.domain.repository.LinksRepository
import com.sakethh.linkora.domain.repository.Message
import com.sakethh.linkora.domain.routes.LinkRoute
import com.sakethh.linkora.domain.tables.LinksTable
import com.sakethh.linkora.domain.tables.LinksTombstone
import com.sakethh.linkora.utils.Result
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.format.DateTimeFormatter

class LinksImplementation : LinksRepository {
    override suspend fun createANewLink(addLinkDTO: AddLinkDTO): Result<NewItemResponseDTO> {
        return try {
            transaction {
                LinksTable.insertAndGetId { link ->
                    link[lastModified] = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
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
                        message = "Link created successfully for ${addLinkDTO.linkType.name} with id = ${idOfNewlyAddedLink}.",
                        id = idOfNewlyAddedLink,
                        correlationId = addLinkDTO.correlationId
                    ), webSocketEvent = WebSocketEvent(
                        operation = LinkRoute.CREATE_A_NEW_LINK.name, payload = Json.encodeToJsonElement(
                            LinkDTO(
                                id = idOfNewlyAddedLink,
                                linkType = addLinkDTO.linkType,
                                title = addLinkDTO.title,
                                url = addLinkDTO.url,
                                baseURL = addLinkDTO.baseURL,
                                imgURL = addLinkDTO.imgURL,
                                note = addLinkDTO.note,
                                lastModified = addLinkDTO.lastModified,
                                idOfLinkedFolder = addLinkDTO.idOfLinkedFolder,
                                userAgent = addLinkDTO.userAgent,
                                markedAsImportant = addLinkDTO.markedAsImportant,
                                mediaType = addLinkDTO.mediaType,
                                correlationId = addLinkDTO.correlationId
                            )
                        )
                    )
                )
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }


    override suspend fun deleteALink(idBasedDTO: IDBasedDTO): Result<Message> {
        return try {
            transaction {
                LinksTable.selectAll().where(LinksTable.id.eq(idBasedDTO.id))
                    .forEach { resultRow ->
                        LinksTombstone.insert(resultRow)
                    }

                LinksTable.deleteWhere {
                    id.eq(idBasedDTO.id)
                }
            }
            Result.Success(
                response = "Link deleted successfully.", webSocketEvent = WebSocketEvent(
                    operation = LinkRoute.DELETE_A_LINK.name, payload = Json.encodeToJsonElement(idBasedDTO),
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun deleteLinksOfAFolder(idBasedDTO: IDBasedDTO): Result<Message> {
        return try {
            transaction {
                LinksTable.selectAll().where(LinksTable.idOfLinkedFolder.eq(idBasedDTO.id))
                    .forEach { resultRow ->
                        LinksTombstone.insert(resultRow)
                    }

                LinksTable.deleteWhere {
                    idOfLinkedFolder.eq(idBasedDTO.id)
                }
            }
            Result.Success(
                response = "Links deleted successfully from the folderId = ${idBasedDTO.id}.", webSocketEvent = null
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun updateLinkedFolderIdOfALink(updateLinkedFolderIDDto: UpdateLinkedFolderIDDto): Result<Message> {
        return try {
            transaction {
                LinksTable.update(where = {
                    LinksTable.id.eq(updateLinkedFolderIDDto.linkId) and LinksTable.linkType.eq(
                        updateLinkedFolderIDDto.linkType.name
                    )
                }) {
                    it[lastModified] = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                    it[idOfLinkedFolder] = updateLinkedFolderIDDto.linkId
                }
            }
            Result.Success(
                response = "idOfLinkedFolder Updated Successfully.", webSocketEvent = WebSocketEvent(
                    operation = LinkRoute.UPDATE_LINKED_FOLDER_ID.name,
                    payload = Json.encodeToJsonElement(updateLinkedFolderIDDto),
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun updateTitleOfTheLink(updateTitleOfTheLinkDTO: UpdateTitleOfTheLinkDTO): Result<Message> {
        return try {
            transaction {
                LinksTable.update(where = {
                    LinksTable.id.eq(updateTitleOfTheLinkDTO.linkId)
                }) {
                    it[lastModified] = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                    it[linkTitle] = updateTitleOfTheLinkDTO.newTitleOfTheLink
                }

            }
            Result.Success(
                response = "Title was updated successfully.", webSocketEvent = WebSocketEvent(
                    operation = LinkRoute.UPDATE_LINK_TITLE.name,
                    payload = Json.encodeToJsonElement(updateTitleOfTheLinkDTO),
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun updateNote(updateNoteOfALinkDTO: UpdateNoteOfALinkDTO): Result<Message> {
        return try {
            transaction {
                LinksTable.update(where = {
                    LinksTable.id.eq(updateNoteOfALinkDTO.linkId)
                }) {
                    it[lastModified] = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                    it[note] = updateNoteOfALinkDTO.newNote
                }
            }
            Result.Success(
                response = "Note was updated successfully.", webSocketEvent = WebSocketEvent(
                    operation = LinkRoute.UPDATE_LINK_NOTE.name,
                    payload = Json.encodeToJsonElement(updateNoteOfALinkDTO),
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun updateUserAgent(updateLinkUserAgentDTO: UpdateLinkUserAgentDTO): Result<Message> {
        return try {
            transaction {
                LinksTable.update(where = {
                    LinksTable.id.eq(updateLinkUserAgentDTO.linkId) and LinksTable.linkType.eq(
                        updateLinkUserAgentDTO.linkType.name
                    )
                }) {
                    it[lastModified] = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                    it[this.userAgent] = userAgent
                }
            }
            Result.Success(
                response = "User agent was updated successfully.", webSocketEvent = WebSocketEvent(
                    operation = LinkRoute.UPDATE_USER_AGENT.name,
                    payload = Json.encodeToJsonElement(updateLinkUserAgentDTO),
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun archiveALink(idBasedDTO: IDBasedDTO): Result<Message> {
        return try {
            transaction {
                LinksTable.update(where = {
                    LinksTable.id.eq(idBasedDTO.id)
                }) {
                    it[linkType] = LinkType.ARCHIVE_LINK.name
                }
            }
            Result.Success(
                response = "Archived link with id : ${idBasedDTO.id} successfully", webSocketEvent = WebSocketEvent(
                    operation = LinkRoute.ARCHIVE_LINK.name, payload = Json.encodeToJsonElement(idBasedDTO)
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun unArchiveALink(idBasedDTO: IDBasedDTO): Result<Message> {
        return try {
            transaction {
                LinksTable.update(where = {
                    LinksTable.id.eq(idBasedDTO.id)
                }) {
                    it[linkType] = LinkType.SAVED_LINK.name
                }
            }
            Result.Success(
                response = "Unarchived link with id : ${idBasedDTO.id} successfully as ${LinkType.SAVED_LINK.name}",
                webSocketEvent = WebSocketEvent(
                    operation = LinkRoute.UNARCHIVE_LINK.name, payload = Json.encodeToJsonElement(idBasedDTO)
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun markALinkAsImp(idBasedDTO: IDBasedDTO): Result<Message> {
        return try {
            transaction {
                LinksTable.update(where = {
                    LinksTable.id.eq(idBasedDTO.id)
                }) {
                    it[markedAsImportant] = true
                }
            }
            Result.Success(
                response = "Marked link with id : ${idBasedDTO.id} as Important.", webSocketEvent = WebSocketEvent(
                    operation = LinkRoute.MARK_AS_IMP.name, payload = Json.encodeToJsonElement(idBasedDTO)
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun markALinkAsNonImp(idBasedDTO: IDBasedDTO): Result<Message> {
        return try {
            transaction {
                LinksTable.update(where = {
                    LinksTable.id.eq(idBasedDTO.id)
                }) {
                    it[markedAsImportant] = false
                }
            }
            Result.Success(
                response = "Marked link with id : ${idBasedDTO.id} as Non-Important.", webSocketEvent = WebSocketEvent(
                    operation = LinkRoute.UNMARK_AS_IMP.name, payload = Json.encodeToJsonElement(idBasedDTO)
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun updateLink(linkDTO: LinkDTO): Result<Message> {
        return try {
            transaction {
                LinksTable.update(where = {
                    LinksTable.id.eq(linkDTO.id)
                }) {
                    it[lastModified] = linkDTO.lastModified
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
                response = "Updated the link (id : ${linkDTO.id}) successfully.",
                webSocketEvent = WebSocketEvent(
                    operation = LinkRoute.UPDATE_LINK.name,
                    payload = Json.encodeToJsonElement(linkDTO)
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
}