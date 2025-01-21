package com.sakethh.linkora.data.repository

import com.sakethh.linkora.LinkoraWebSocket
import com.sakethh.linkora.domain.Folder
import com.sakethh.linkora.domain.dto.folder.ChangeParentFolderDTO
import com.sakethh.linkora.domain.dto.folder.AddFolderDTO
import com.sakethh.linkora.domain.dto.folder.UpdateFolderNameDTO
import com.sakethh.linkora.domain.dto.folder.UpdateFolderNoteDTO
import com.sakethh.linkora.domain.dto.link.NewItemResponseDTO
import com.sakethh.linkora.domain.model.ChangeNotification
import com.sakethh.linkora.domain.repository.FoldersRepository
import com.sakethh.linkora.domain.repository.LinksRepository
import com.sakethh.linkora.domain.repository.Message
import com.sakethh.linkora.domain.routes.FolderRoute
import com.sakethh.linkora.domain.tables.FoldersTable
import com.sakethh.linkora.utils.Result
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.format.DateTimeFormatter

class FoldersImplementation(private val linksRepository: LinksRepository) : FoldersRepository {
    override suspend fun createFolder(addFolderDTO: AddFolderDTO): Result<NewItemResponseDTO> {
        return try {
            transaction {
                FoldersTable.insertAndGetId { folder ->
                    folder[lastModified] = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                    folder[folderName] = addFolderDTO.name
                    folder[infoForSaving] = addFolderDTO.note
                    folder[parentFolderID] = addFolderDTO.parentFolderId
                    folder[isFolderArchived] = addFolderDTO.isArchived
                }
            }.value.let {
                LinkoraWebSocket.sendNotification(
                    ChangeNotification(
                        operation = FolderRoute.CREATE_FOLDER.name,
                        payload = Json.encodeToJsonElement(Folder(
                            id = it,
                            name = addFolderDTO.name,
                            note = addFolderDTO.note,
                            parentFolderId = addFolderDTO.parentFolderId,
                            isArchived = addFolderDTO.isArchived
                        ))
                    )
                )
                Result.Success(NewItemResponseDTO(message = "Folder created successfully with id = $it", id = it))
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun deleteFolder(folderId: Long): Result<Message> {
        return try {
            transaction {
                FoldersTable.deleteWhere {
                    FoldersTable.id.eq(folderId)
                }
            }
            when (val childFolders = getChildFolders(folderId)) {
                is Result.Failure -> {
                    throw childFolders.exception
                }

                is Result.Success -> {
                    childFolders.result.map { it.id }.forEach { childFolderId ->
                        childFolderId?.let {
                            deleteFolder(it)
                        }
                    }
                }
            }
            Result.Success("Folder and its contents have been successfully deleted.")
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun getChildFolders(parentFolderId: Long): Result<List<Folder>> {
        return try {
            transaction {
                FoldersTable.selectAll().where {
                    FoldersTable.parentFolderID.eq(parentFolderId)
                }.toList().map {
                    Folder(
                        id = it[FoldersTable.id].value,
                        name = it[FoldersTable.folderName],
                        note = it[FoldersTable.infoForSaving],
                        parentFolderId = it[FoldersTable.parentFolderID],
                        isArchived = it[FoldersTable.isFolderArchived]
                    )
                }
            }.let {
                Result.Success(it)
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun getRootFolders(): Result<List<Folder>> {
        return try {
            transaction {
                FoldersTable.selectAll().where {
                    FoldersTable.parentFolderID.eq(null) and FoldersTable.isFolderArchived.eq(false)
                }.toList().map {
                    Folder(
                        id = it[FoldersTable.id].value,
                        name = it[FoldersTable.folderName],
                        note = it[FoldersTable.infoForSaving],
                        parentFolderId = it[FoldersTable.parentFolderID],
                        isArchived = it[FoldersTable.isFolderArchived]
                    )
                }
            }.let {
                Result.Success(it)
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun markAsArchive(folderId: Long): Result<Message> {
        return try {
            transaction {
                FoldersTable.update(where = { FoldersTable.id.eq(folderId) }) {
                    it[lastModified] = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                    it[isFolderArchived] = true
                }
            }.let {
                LinkoraWebSocket.sendNotification(
                    ChangeNotification(
                        operation = FolderRoute.MARK_AS_ARCHIVE.name,
                        payload = Json.encodeToJsonElement(folderId)
                    )
                )
                Result.Success("Number of rows affected by the update = $it")
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun markAsRegularFolder(folderId: Long): Result<Message> {
        return try {
            transaction {
                FoldersTable.update(where = { FoldersTable.id.eq(folderId) }) {
                    it[lastModified] = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                    it[isFolderArchived] = false
                }
            }.let {
                LinkoraWebSocket.sendNotification(
                    ChangeNotification(
                        operation = FolderRoute.MARK_AS_REGULAR_FOLDER.name,
                        payload = Json.encodeToJsonElement(folderId)
                    )
                )
                Result.Success("Number of rows affected by the update = $it")
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun changeParentFolder(folderId: Long, newParentFolderId: Long): Result<Message> {
        return try {
            transaction {
                FoldersTable.update(where = { FoldersTable.id.eq(folderId) }) {
                    it[lastModified] = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                    it[parentFolderID] = newParentFolderId
                }
            }.let {
                LinkoraWebSocket.sendNotification(
                    ChangeNotification(
                        operation = FolderRoute.CHANGE_PARENT_FOLDER.name,
                        payload = Json.encodeToJsonElement(ChangeParentFolderDTO(folderId, newParentFolderId))
                    )
                )
                Result.Success("Number of rows affected by the update = $it")
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun updateFolderName(folderId: Long, newFolderName: String): Result<Message> {
        return try {
            transaction {
                FoldersTable.update(where = { FoldersTable.id.eq(folderId) }) {
                    it[lastModified] = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                    it[folderName] = newFolderName
                }
            }.let {
                LinkoraWebSocket.sendNotification(
                    ChangeNotification(
                        operation = FolderRoute.UPDATE_FOLDER_NAME.name,
                        payload = Json.encodeToJsonElement(UpdateFolderNameDTO(folderId, newFolderName))
                    )
                )
                Result.Success("Number of rows affected by the update = $it")
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun updateFolderNote(folderId: Long, newNote: String): Result<Message> {
        return try {
            transaction {
                FoldersTable.update(where = { FoldersTable.id.eq(folderId) }) {
                    it[lastModified] = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                    it[infoForSaving] = newNote
                }
            }.let {
                LinkoraWebSocket.sendNotification(
                    ChangeNotification(
                        operation = FolderRoute.UPDATE_FOLDER_NOTE.name,
                        payload = Json.encodeToJsonElement(UpdateFolderNoteDTO(folderId, newNote))
                    )
                )
                Result.Success("Number of rows affected by the update = $it")
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun deleteFolderNote(folderId: Long): Result<Message> {
        return try {
            transaction {
                FoldersTable.update(where = { FoldersTable.id.eq(folderId) }) {
                    it[infoForSaving] = ""
                    it[lastModified] = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                }
            }.let {
                LinkoraWebSocket.sendNotification(
                    ChangeNotification(
                        operation = FolderRoute.DELETE_FOLDER_NOTE.name,
                        payload = Json.encodeToJsonElement(folderId)
                    )
                )
                Result.Success("Number of rows affected by the update = $it")
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
}