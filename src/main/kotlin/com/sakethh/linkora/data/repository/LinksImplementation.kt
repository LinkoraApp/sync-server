package com.sakethh.linkora.data.repository

import com.sakethh.linkora.domain.Link
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.dto.link.*
import com.sakethh.linkora.domain.handler.LinksTombstoneHandler.insert
import com.sakethh.linkora.domain.mapper.LinksMapper
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

class LinksImplementation(
    private val linksMapper: LinksMapper = LinksMapper()
) : LinksRepository {
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
                /* TODO LinkoraWebSocket.sendNotification(
                    ChangeNotification(
                        operation = LinkRoute.CREATE_A_NEW_LINK.name,
                        payload = Json.encodeToJsonElement(addLinkDTO.copy(id = idOfNewlyAddedLink))
                    )
                )*/
                Result.Success(
                    response = NewItemResponseDTO(
                        message = "Link created successfully for ${addLinkDTO.linkType.name} with id = ${idOfNewlyAddedLink}.",
                        id = idOfNewlyAddedLink
                    ), webSocketEvent = null
                )
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }


    override suspend fun deleteALink(linkId: Long): Result<Message> {
        return try {
            transaction {
                LinksTable.selectAll()
                    .where(LinksTable.id.eq(linkId))
                    .forEach { resultRow ->
                        LinksTombstone.insert(resultRow)
                    }

                LinksTable.deleteWhere {
                    id.eq(linkId)
                }
            }
            Result.Success(
                response = "Link deleted successfully.", webSocketEvent = WebSocketEvent(
                    operation = LinkRoute.DELETE_A_LINK.name, payload = Json.encodeToJsonElement(linkId)
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun deleteLinksOfAFolder(folderId: Long): Result<Message> {
        return try {
            transaction {
                LinksTable.selectAll()
                    .where(LinksTable.idOfLinkedFolder.eq(folderId))
                    .forEach { resultRow ->
                        LinksTombstone.insert(resultRow)
                    }

                LinksTable.deleteWhere {
                    idOfLinkedFolder.eq(folderId)
                }
            }
            Result.Success("Links deleted successfully from the folderId = $folderId.")
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
                    payload = Json.encodeToJsonElement(updateLinkedFolderIDDto)
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
                    payload = Json.encodeToJsonElement(updateTitleOfTheLinkDTO)
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
                    payload = Json.encodeToJsonElement(updateNoteOfALinkDTO)
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
                    payload = Json.encodeToJsonElement(updateLinkUserAgentDTO)
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun archiveALink(linkId: Long): Result<Message> {
        return try {
            transaction {
                LinksTable.update(where = {
                    LinksTable.id.eq(linkId)
                }) {
                    it[linkType] = LinkType.ARCHIVE_LINK.name
                }
            }
            Result.Success("Archived link with id : $linkId successfully")
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun unArchiveALink(linkId: Long): Result<Message> {
        return try {
            transaction {
                LinksTable.update(where = {
                    LinksTable.id.eq(linkId)
                }) {
                    it[linkType] = LinkType.SAVED_LINK.name
                }
            }
            Result.Success("Unarchived link with id : $linkId successfully as ${LinkType.SAVED_LINK.name}")
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun markALinkAsImp(linkId: Long): Result<Message> {
        return try {
            transaction {
                LinksTable.update(where = {
                    LinksTable.id.eq(linkId)
                }) {
                    it[markedAsImportant] = true
                }
            }
            Result.Success("Marked link with id : $linkId as Important.")
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun markALinkAsNonImp(linkId: Long): Result<Message> {
        return try {
            transaction {
                LinksTable.update(where = {
                    LinksTable.id.eq(linkId)
                }) {
                    it[markedAsImportant] = false
                }
            }
            Result.Success("Marked link with id : $linkId as Non-Important.")
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun updateLink(link: Link): Result<Message> {
        return try {
            transaction {
                LinksTable.update(where = {
                    LinksTable.id.eq(link.id)
                }) {
                    it[lastModified] = link.lastModified
                    it[linkType] = link.linkType.name
                    it[linkTitle] = link.title
                    it[url] = link.url
                    it[baseURL] = link.baseURL
                    it[imgURL] = link.imgURL
                    it[note] = link.note
                    it[idOfLinkedFolder] = link.idOfLinkedFolder
                    it[userAgent] = link.userAgent
                    it[mediaType] = link.mediaType.name
                    it[markedAsImportant] = link.markedAsImportant
                }
            }
            Result.Success("Updated the link (id : ${link.id}) successfully.")
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
}