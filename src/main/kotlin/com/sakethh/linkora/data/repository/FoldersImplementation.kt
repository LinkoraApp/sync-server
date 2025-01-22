package com.sakethh.linkora.data.repository

import com.sakethh.linkora.domain.Folder
import com.sakethh.linkora.domain.dto.folder.AddFolderDTO
import com.sakethh.linkora.domain.dto.folder.ChangeParentFolderDTO
import com.sakethh.linkora.domain.dto.folder.UpdateFolderNameDTO
import com.sakethh.linkora.domain.dto.folder.UpdateFolderNoteDTO
import com.sakethh.linkora.domain.dto.link.NewItemResponseDTO
import com.sakethh.linkora.domain.model.WebSocketEvent
import com.sakethh.linkora.domain.repository.FoldersRepository
import com.sakethh.linkora.domain.repository.LinksRepository
import com.sakethh.linkora.domain.repository.Message
import com.sakethh.linkora.domain.routes.FolderRoute
import com.sakethh.linkora.domain.tables.FoldersTable
import com.sakethh.linkora.domain.tables.PanelFoldersTable
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
                Result.Success(
                    response = NewItemResponseDTO(message = "Folder created successfully with id = $it", id = it),
                    webSocketEvent = WebSocketEvent(
                        operation = FolderRoute.CREATE_FOLDER.name, payload = Json.encodeToJsonElement(
                            Folder(
                                id = it,
                                name = addFolderDTO.name,
                                note = addFolderDTO.note,
                                parentFolderId = addFolderDTO.parentFolderId,
                                isArchived = addFolderDTO.isArchived
                            )
                        )
                    )
                )
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
                    childFolders.response.map { it.id }.forEach { childFolderId ->
                        childFolderId?.let {
                            deleteFolder(it)
                        }
                    }
                }
            }
            Result.Success(
                response = "Folder and its contents have been successfully deleted.", webSocketEvent = WebSocketEvent(
                    operation = FolderRoute.DELETE_FOLDER.name,
                    payload = Json.encodeToJsonElement(folderId)
                )
            )
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
                Result.Success(response = it, webSocketEvent = null)
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
                Result.Success(response = it, webSocketEvent = null)
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
                Result.Success(
                    response = "Number of rows affected by the update = $it", webSocketEvent = WebSocketEvent(
                        operation = FolderRoute.MARK_AS_ARCHIVE.name,
                        payload = Json.encodeToJsonElement(folderId)
                    )
                )
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
                Result.Success(
                    response = "Number of rows affected by the update = $it", webSocketEvent = WebSocketEvent(
                        operation = FolderRoute.MARK_AS_REGULAR_FOLDER.name,
                        payload = Json.encodeToJsonElement(folderId)
                    )
                )
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
                Result.Success(
                    response = "Number of rows affected by the update = $it", webSocketEvent = WebSocketEvent(
                        operation = FolderRoute.CHANGE_PARENT_FOLDER.name,
                        payload = Json.encodeToJsonElement(ChangeParentFolderDTO(folderId, newParentFolderId))
                    )
                )
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
                PanelFoldersTable.update(where = {
                    PanelFoldersTable.folderId.eq(folderId)
                }) {
                    it[folderName] = newFolderName
                }
            }.let {
                Result.Success(
                    response = "Number of rows affected by the update = $it", webSocketEvent = WebSocketEvent(
                        operation = FolderRoute.UPDATE_FOLDER_NAME.name,
                        payload = Json.encodeToJsonElement(UpdateFolderNameDTO(folderId, newFolderName))
                    )
                )
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
                Result.Success(
                    response = "Number of rows affected by the update = $it", webSocketEvent = WebSocketEvent(
                        operation = FolderRoute.UPDATE_FOLDER_NOTE.name,
                        payload = Json.encodeToJsonElement(UpdateFolderNoteDTO(folderId, newNote))
                    )
                )
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
                Result.Success(
                    response = "Number of rows affected by the update = $it", webSocketEvent = WebSocketEvent(
                        operation = FolderRoute.DELETE_FOLDER_NOTE.name,
                        payload = Json.encodeToJsonElement(folderId)
                    )
                )
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
}