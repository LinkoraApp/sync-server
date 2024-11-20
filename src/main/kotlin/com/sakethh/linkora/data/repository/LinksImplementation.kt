package com.sakethh.linkora.data.repository

import com.sakethh.linkora.LinkoraWebSocket
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.dto.link.*
import com.sakethh.linkora.domain.mapper.LinksMapper
import com.sakethh.linkora.domain.model.ChangeNotification
import com.sakethh.linkora.domain.repository.LinksRepository
import com.sakethh.linkora.domain.repository.Message
import com.sakethh.linkora.domain.routes.LinkRoute
import com.sakethh.linkora.domain.tables.LinksTable
import com.sakethh.linkora.utils.RequestResultState
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class LinksImplementation(
    private val linksMapper: LinksMapper = LinksMapper()
) : LinksRepository {
    override suspend fun createANewLink(linkDTO: LinkDTO): RequestResultState<Message> {
        return try {
            transaction {
                LinksTable.insert { link ->
                    link[linkType] = linkDTO.linkType.name
                    link[linkTitle] = linkDTO.linkTitle
                    link[webURL] = linkDTO.webURL
                    link[baseURL] = linkDTO.baseURL
                    link[imgURL] = linkDTO.imgURL
                    link[infoForSaving] = linkDTO.infoForSaving
                    link[isLinkedWithSavedLinks] = linkDTO.isLinkedWithSavedLinks
                    link[isLinkedWithFolders] = linkDTO.isLinkedWithFolders
                    link[idOfLinkedFolder] = linkDTO.idOfLinkedFolder
                    link[userAgent] = linkDTO.userAgent
                    link[id] = linkDTO.id
                }
            }.let {
                LinkoraWebSocket.sendNotification(
                    ChangeNotification(
                        operation = LinkRoute.CREATE_A_NEW_LINK.name, payload = linkDTO
                    )
                )
                RequestResultState.Success("Link created successfully for ${linkDTO.linkType.name} with id = ${linkDTO.id}.")
            }
        } catch (e: Exception) {
            RequestResultState.Failure(e)
        }
    }


    override suspend fun deleteALink(deleteALinkDTO: DeleteALinkDTO): RequestResultState<Message> {
        return try {
            transaction {
                LinksTable.deleteWhere {
                    id.eq(deleteALinkDTO.linkId) and linkType.eq(deleteALinkDTO.linkType.name)
                }
            }
            LinkoraWebSocket.sendNotification(
                ChangeNotification(
                    operation = LinkRoute.DELETE_A_LINK.name, payload = deleteALinkDTO
                )
            )
            RequestResultState.Success("Link deleted successfully.")
        } catch (e: Exception) {
            RequestResultState.Failure(e)
        }
    }

    override suspend fun deleteLinksOfAFolder(folderId: Long): RequestResultState<Message> {
        return try {
            transaction {
                LinksTable.deleteWhere {
                    idOfLinkedFolder.eq(folderId)
                }
            }
            RequestResultState.Success("Links deleted successfully from the folderId = $folderId.")
        } catch (e: Exception) {
            RequestResultState.Failure(e)
        }
    }

    override suspend fun updateLinkedFolderIdOfALink(updateLinkedFolderIDDto: UpdateLinkedFolderIDDto): RequestResultState<Message> {
        return try {
            transaction {
                LinksTable.update(where = {
                    LinksTable.id.eq(updateLinkedFolderIDDto.linkId) and LinksTable.linkType.eq(
                        updateLinkedFolderIDDto.linkType.name
                    )
                }) {
                    it[idOfLinkedFolder] = updateLinkedFolderIDDto.linkId
                }
            }
            LinkoraWebSocket.sendNotification(
                ChangeNotification(
                    operation = LinkRoute.UPDATE_LINKED_FOLDER_ID.name, payload = updateLinkedFolderIDDto
                )
            )
            RequestResultState.Success("idOfLinkedFolder Updated Successfully.")
        } catch (e: Exception) {
            RequestResultState.Failure(e)
        }
    }

    override suspend fun updateTitleOfTheLink(updateTitleOfTheLinkDTO: UpdateTitleOfTheLinkDTO): RequestResultState<Message> {
        return try {
            transaction {
                LinksTable.update(where = {
                    LinksTable.id.eq(updateTitleOfTheLinkDTO.linkId) and LinksTable.linkType.eq(
                        updateTitleOfTheLinkDTO.linkType.name
                    )
                }) {
                    it[linkTitle] = updateTitleOfTheLinkDTO.newTitleOfTheLink
                }

            }
            LinkoraWebSocket.sendNotification(
                ChangeNotification(
                    operation = LinkRoute.UPDATE_LINK_TITLE.name, payload = updateTitleOfTheLinkDTO
                )
            )
            RequestResultState.Success("Title was updated successfully.")
        } catch (e: Exception) {
            RequestResultState.Failure(e)
        }
    }

    override suspend fun updateNote(updateNoteOfALinkDTO: UpdateNoteOfALinkDTO): RequestResultState<Message> {
        return try {
            transaction {
                LinksTable.update(where = {
                    LinksTable.id.eq(updateNoteOfALinkDTO.linkId) and LinksTable.linkType.eq(
                        updateNoteOfALinkDTO.linkType.name
                    )
                }) {
                    it[infoForSaving] = updateNoteOfALinkDTO.newNote
                }
            }
            LinkoraWebSocket.sendNotification(
                ChangeNotification(
                    operation = LinkRoute.UPDATE_LINK_NOTE.name, payload = updateNoteOfALinkDTO
                )
            )
            RequestResultState.Success("Note was updated successfully.")
        } catch (e: Exception) {
            RequestResultState.Failure(e)
        }
    }

    override suspend fun updateUserAgent(updateLinkUserAgentDTO: UpdateLinkUserAgentDTO): RequestResultState<Message> {
        return try {
            transaction {
                LinksTable.update(where = {
                    LinksTable.id.eq(updateLinkUserAgentDTO.linkId) and LinksTable.linkType.eq(
                        updateLinkUserAgentDTO.linkType.name
                    )
                }) {
                    it[this.userAgent] = userAgent
                }
            }
            LinkoraWebSocket.sendNotification(
                ChangeNotification(
                    operation = LinkRoute.UPDATE_USER_AGENT.name, payload = updateLinkUserAgentDTO
                )
            )
            RequestResultState.Success("User agent was updated successfully.")
        } catch (e: Exception) {
            RequestResultState.Failure(e)
        }
    }

    override suspend fun getLinks(linkType: LinkType): RequestResultState<List<LinkDTO>> {
        return try {
            transaction {
                LinksTable.selectAll().where {
                    LinksTable.linkType.eq(linkType.name)
                }.let {
                    linksMapper.toDto(it)
                }
            }.let {
                RequestResultState.Success(it)
            }
        } catch (e: Exception) {
            RequestResultState.Failure(e)
        }
    }

    override suspend fun getLinksFromAFolder(folderId: Long): RequestResultState<List<LinkDTO>> {
        return try {
            transaction {
                LinksTable.selectAll().where {
                    LinksTable.isLinkedWithFolders.eq(true) and LinksTable.idOfLinkedFolder.eq(
                        folderId
                    )
                }.let {
                    linksMapper.toDto(it)
                }
            }.let {
                RequestResultState.Success(it)
            }
        } catch (e: Exception) {
            RequestResultState.Failure(e)
        }
    }
}