package com.sakethh.linkora.data.repository

import com.sakethh.linkora.domain.dto.FolderDTO
import com.sakethh.linkora.domain.repository.FoldersRepository
import com.sakethh.linkora.domain.repository.Message
import com.sakethh.linkora.domain.tables.FoldersTable
import com.sakethh.linkora.utils.RequestResultState
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class FoldersImplementation : FoldersRepository {
    override suspend fun createFolder(folderDTO: FolderDTO): RequestResultState<Message> {
        return try {
            transaction {
                FoldersTable.insertAndGetId { folder ->
                    folder[folderName] = folderDTO.folderName
                    folder[infoForSaving] = folderDTO.infoForSaving
                    folder[parentFolderID] = folderDTO.parentFolderID
                    folder[isFolderArchived] = folderDTO.isFolderArchived
                }
            }.value.let {
                RequestResultState.Success("Folder created successfully with id = $it")
            }
        } catch (e: Exception) {
            RequestResultState.Failure(e)
        }
    }

    override suspend fun deleteFolder(folderId: Long): RequestResultState<Message> {
        return try {
            transaction {
                FoldersTable.deleteWhere {
                    FoldersTable.id.eq(folderId)
                }
            }.let {
                RequestResultState.Success("Count of deleted rows = $it")
            }
        } catch (e: Exception) {
            RequestResultState.Failure(e)
        }
    }

    override suspend fun getChildFolders(parentFolderId: Long): RequestResultState<List<FolderDTO>> {
        return try {
            FoldersTable.selectAll().where {
                FoldersTable.parentFolderID.eq(parentFolderId)
            }.toList().map {
                FolderDTO(
                    id = it[FoldersTable.id].value,
                    folderName = it[FoldersTable.folderName],
                    infoForSaving = it[FoldersTable.infoForSaving],
                    parentFolderID = it[FoldersTable.parentFolderID],
                    isFolderArchived = it[FoldersTable.isFolderArchived]
                )
            }.let {
                RequestResultState.Success(it)
            }
        } catch (e: Exception) {
            RequestResultState.Failure(e)
        }
    }

    override suspend fun getRootFolders(): RequestResultState<List<FolderDTO>> {
        return try {
            transaction {
                FoldersTable.selectAll().where {
                    FoldersTable.parentFolderID.eq(null) and FoldersTable.isFolderArchived.eq(false)
                }.toList().map {
                    FolderDTO(
                        id = it[FoldersTable.id].value,
                        folderName = it[FoldersTable.folderName],
                        infoForSaving = it[FoldersTable.infoForSaving],
                        parentFolderID = it[FoldersTable.parentFolderID],
                        isFolderArchived = it[FoldersTable.isFolderArchived]
                    )
                }
            }.let {
                RequestResultState.Success(it)
            }
        } catch (e: Exception) {
            RequestResultState.Failure(e)
        }
    }

    override suspend fun markAsArchive(folderId: Long): RequestResultState<Message> {
        return try {
            transaction {
                FoldersTable.update(where = { FoldersTable.id.eq(folderId) }) {
                    it[isFolderArchived] = true
                }
            }.let {
                RequestResultState.Success("Number of rows affected by the update = $it")
            }
        } catch (e: Exception) {
            RequestResultState.Failure(e)
        }
    }

    override suspend fun markAsRegularFolder(folderId: Long): RequestResultState<Message> {
        return try {
            transaction {
                FoldersTable.update(where = { FoldersTable.id.eq(folderId) }) {
                    it[isFolderArchived] = false
                }
            }.let {
                RequestResultState.Success("Number of rows affected by the update = $it")
            }
        } catch (e: Exception) {
            RequestResultState.Failure(e)
        }
    }

    override suspend fun changeParentFolder(folderId: Long, newParentFolderId: Long): RequestResultState<Message> {
        return try {
            transaction {
                FoldersTable.update(where = { FoldersTable.id.eq(folderId) }) {
                    it[parentFolderID] = newParentFolderId
                }
            }.let {
                RequestResultState.Success("Number of rows affected by the update = $it")
            }
        } catch (e: Exception) {
            RequestResultState.Failure(e)
        }
    }

    override suspend fun updateFolderName(folderId: Long, newFolderName: String): RequestResultState<Message> {
        return try {
            transaction {
                FoldersTable.update(where = { FoldersTable.id.eq(folderId) }) {
                    it[folderName] = newFolderName
                }
            }.let {
                RequestResultState.Success("Number of rows affected by the update = $it")
            }
        } catch (e: Exception) {
            RequestResultState.Failure(e)
        }
    }

    override suspend fun updateFolderNote(folderId: Long, newNote: String): RequestResultState<Message> {
        return try {
            transaction {
                FoldersTable.update(where = { FoldersTable.id.eq(folderId) }) {
                    it[infoForSaving] = newNote
                }
            }.let {
                RequestResultState.Success("Number of rows affected by the update = $it")
            }
        } catch (e: Exception) {
            RequestResultState.Failure(e)
        }
    }

    override suspend fun deleteFolderNote(folderId: Long): RequestResultState<Message> {
        return try {
            transaction {
                FoldersTable.update(where = { FoldersTable.id.eq(folderId) }) {
                    it[infoForSaving] = ""
                }
            }.let {
                RequestResultState.Success("Number of rows affected by the update = $it")
            }
        } catch (e: Exception) {
            RequestResultState.Failure(e)
        }
    }
}