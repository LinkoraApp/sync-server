package com.sakethh.linkora.data.repository

import com.sakethh.linkora.domain.dto.SavedAndFolderLinksDTO
import com.sakethh.linkora.domain.mapper.SavedFolderLinksMapper
import com.sakethh.linkora.domain.repository.Message
import com.sakethh.linkora.domain.repository.SavedAndFolderLinksRepository
import com.sakethh.linkora.domain.tables.SavedAndFolderLinksTable
import com.sakethh.linkora.utils.RequestResultState
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class SavedAndFolderLinksImplementation(
    private val savedFolderLinksMapper: SavedFolderLinksMapper = SavedFolderLinksMapper()
) : SavedAndFolderLinksRepository {
    override suspend fun createANewLink(savedAndFolderLinksDTO: SavedAndFolderLinksDTO): RequestResultState<Message> {
        return try {
            transaction {
                SavedAndFolderLinksTable.insertAndGetId { link ->
                    link[linkTitle] = savedAndFolderLinksDTO.linkTitle
                    link[webURL] = savedAndFolderLinksDTO.webURL
                    link[baseURL] = savedAndFolderLinksDTO.baseURL
                    link[imgURL] = savedAndFolderLinksDTO.imgURL
                    link[infoForSaving] = savedAndFolderLinksDTO.infoForSaving
                    link[isLinkedWithSavedLinks] = savedAndFolderLinksDTO.isLinkedWithSavedLinks
                    link[isLinkedWithFolders] = savedAndFolderLinksDTO.isLinkedWithFolders
                    link[idOfLinkedFolder] = savedAndFolderLinksDTO.idOfLinkedFolder
                    link[userAgent] = savedAndFolderLinksDTO.userAgent
                }
            }.let {
                RequestResultState.Success("Link created successfully in ${if (savedAndFolderLinksDTO.isLinkedWithSavedLinks) "Saved Links" else "Folder Links"} with id = ${it.value}.")
            }
        } catch (e: Exception) {
            RequestResultState.Failure(e)
        }
    }


    override suspend fun deleteALink(linkId: Long): RequestResultState<Message> {
        return try {
            transaction {
                SavedAndFolderLinksTable.deleteWhere {
                    id.eq(linkId)
                }.let {
                    RequestResultState.Success("Link deleted successfully.")
                }
            }
        } catch (e: Exception) {
            RequestResultState.Failure(e)
        }
    }

    override suspend fun deleteLinksOfAFolder(folderId: Long): RequestResultState<Message> {
        return try {
            transaction {
                SavedAndFolderLinksTable.deleteWhere {
                    idOfLinkedFolder.eq(folderId)
                }.let {
                    RequestResultState.Success("Links deleted successfully from the folderId = $folderId.")
                }
            }
        } catch (e: Exception) {
            RequestResultState.Failure(e)
        }
    }

    override suspend fun updateLinkedFolderId(linkId: Long, newParentFolderId: Long): RequestResultState<Message> {
        return try {
            transaction {
                SavedAndFolderLinksTable.update(where = { SavedAndFolderLinksTable.id.eq(linkId) }) {
                    it[idOfLinkedFolder] = newParentFolderId
                }.let {
                    RequestResultState.Success("idOfLinkedFolder Updated Successfully.")
                }
            }
        } catch (e: Exception) {
            RequestResultState.Failure(e)
        }
    }

    override suspend fun updateTitleOfTheLink(linkId: Long, newTitleOfTheLink: String): RequestResultState<Message> {
        return try {
            transaction {
                SavedAndFolderLinksTable.update(where = { SavedAndFolderLinksTable.id.eq(linkId) }) {
                    it[linkTitle] = newTitleOfTheLink
                }.let {
                    RequestResultState.Success("Title was updated successfully.")
                }
            }
        } catch (e: Exception) {
            RequestResultState.Failure(e)
        }
    }

    override suspend fun updateNote(linkId: Long, newNote: String): RequestResultState<Message> {
        return try {
            transaction {
                SavedAndFolderLinksTable.update(where = { SavedAndFolderLinksTable.id.eq(linkId) }) {
                    it[infoForSaving] = newNote
                }.let {
                    RequestResultState.Success("Note was updated successfully.")
                }
            }
        } catch (e: Exception) {
            RequestResultState.Failure(e)
        }
    }

    override suspend fun deleteNote(linkId: Long): RequestResultState<Message> {
        return try {
            transaction {
                SavedAndFolderLinksTable.update(where = { SavedAndFolderLinksTable.id.eq(linkId) }) {
                    it[infoForSaving] = ""
                }.let {
                    RequestResultState.Success("Note was deleted successfully.")
                }
            }
        } catch (e: Exception) {
            RequestResultState.Failure(e)
        }
    }

    override suspend fun updateUserAgent(linkId: Long, userAgent: String): RequestResultState<Message> {
        return try {
            transaction {
                SavedAndFolderLinksTable.update(where = { SavedAndFolderLinksTable.id.eq(linkId) }) {
                    it[this.userAgent] = userAgent
                }.let {
                    RequestResultState.Success("User agent was updated successfully.")
                }
            }
        } catch (e: Exception) {
            RequestResultState.Failure(e)
        }
    }

    override suspend fun getAllLinks(): RequestResultState<List<SavedAndFolderLinksDTO>> {
        return try {
            transaction {
                SavedAndFolderLinksTable.selectAll().let {
                    savedFolderLinksMapper.toDto(it)
                }
            }.let {
                RequestResultState.Success(it)
            }
        } catch (e: Exception) {
            RequestResultState.Failure(e)
        }
    }

    override suspend fun getSavedLinks(): RequestResultState<List<SavedAndFolderLinksDTO>> {
        return try {
            transaction {
                SavedAndFolderLinksTable.selectAll().where {
                    SavedAndFolderLinksTable.isLinkedWithSavedLinks.eq(true) and SavedAndFolderLinksTable.isLinkedWithFolders.eq(
                        false
                    )
                }.let {
                    savedFolderLinksMapper.toDto(it)
                }
            }.let {
                RequestResultState.Success(it)
            }
        } catch (e: Exception) {
            RequestResultState.Failure(e)
        }
    }

    override suspend fun getLinksFromAFolder(folderId: Long): RequestResultState<List<SavedAndFolderLinksDTO>> {
        return try {
            transaction {
                SavedAndFolderLinksTable.selectAll().where {
                    SavedAndFolderLinksTable.isLinkedWithFolders.eq(true) and SavedAndFolderLinksTable.idOfLinkedFolder.eq(
                        folderId
                    )
                }.let {
                    savedFolderLinksMapper.toDto(it)
                }
            }.let {
                RequestResultState.Success(it)
            }
        } catch (e: Exception) {
            RequestResultState.Failure(e)
        }
    }
}