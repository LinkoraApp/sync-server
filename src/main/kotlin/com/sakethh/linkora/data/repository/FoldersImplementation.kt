package com.sakethh.linkora.data.repository

import com.sakethh.linkora.domain.dto.FolderDTO
import com.sakethh.linkora.domain.repository.FoldersRepository
import com.sakethh.linkora.domain.tables.FoldersTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class FoldersImplementation : FoldersRepository {
    override suspend fun createFolder(folderDTO: FolderDTO): Long {
        return transaction {
            return@transaction FoldersTable.insertAndGetId { folder ->
                folder[folderName] = folderDTO.folderName
                folder[infoForSaving] = folderDTO.infoForSaving
                folder[parentFolderID] = folderDTO.parentFolderID
                folder[isFolderArchived] = folderDTO.isFolderArchived
            }
        }.value
    }

    override suspend fun deleteFolder(folderId: Long) {
        transaction {
            FoldersTable.deleteWhere {
                FoldersTable.id.eq(folderId)
            }
        }
    }

    override suspend fun getChildFolders(parentFolderId: Long): List<FolderDTO> {
        return FoldersTable.selectAll().where {
            FoldersTable.parentFolderID.eq(parentFolderId)
        }.toList().map {
            FolderDTO(
                id = it[FoldersTable.id].value,
                folderName = it[FoldersTable.folderName],
                infoForSaving = it[FoldersTable.infoForSaving],
                parentFolderID = it[FoldersTable.parentFolderID],
                isFolderArchived = it[FoldersTable.isFolderArchived]
            )
        }
    }

    override suspend fun getRootFolders(): List<FolderDTO> {
        return transaction {
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
        }
    }

    override suspend fun markAsArchive(folderId: Long) {
        transaction {
            FoldersTable.update(where = { FoldersTable.id.eq(folderId) }) {
                it[isFolderArchived] = true
            }
        }
    }

    override suspend fun markAsRegularFolder(folderId: Long) {
        transaction {
            FoldersTable.update(where = { FoldersTable.id.eq(folderId) }) {
                it[isFolderArchived] = false
            }
        }
    }

    override suspend fun changeParentFolder(folderId: Long, newParentFolderId: Long) {
        transaction {
            FoldersTable.update(where = { FoldersTable.id.eq(folderId) }) {
                it[parentFolderID] = newParentFolderId
            }
        }
    }

    override suspend fun updateFolderName(folderId: Long, newFolderName: String) {
        transaction {
            FoldersTable.update(where = { FoldersTable.id.eq(folderId) }) {
                it[folderName] = newFolderName
            }
        }
    }

    override suspend fun updateFolderNote(folderId: Long, note: String) {
        transaction {
            FoldersTable.update(where = { FoldersTable.id.eq(folderId) }) {
                it[infoForSaving] = note
            }
        }
    }

    override suspend fun deleteFolderNote(folderId: Long) {
        transaction {
            FoldersTable.update(where = { FoldersTable.id.eq(folderId) }) {
                it[infoForSaving] = ""
            }
        }
    }
}