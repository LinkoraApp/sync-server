package com.sakethh.linkora.data.repository

import com.sakethh.linkora.domain.Folder
import com.sakethh.linkora.domain.dto.IDBasedDTO
import com.sakethh.linkora.domain.dto.folder.*
import com.sakethh.linkora.domain.dto.NewItemResponseDTO
import com.sakethh.linkora.domain.model.WebSocketEvent
import com.sakethh.linkora.domain.repository.FoldersRepository
import com.sakethh.linkora.domain.repository.LinksRepository
import com.sakethh.linkora.domain.repository.Message
import com.sakethh.linkora.domain.routes.FolderRoute
import com.sakethh.linkora.domain.routes.PanelRoute
import com.sakethh.linkora.domain.tables.FoldersTable
import com.sakethh.linkora.domain.tables.PanelFoldersTable
import com.sakethh.linkora.domain.tables.helper.TombStoneHelper
import com.sakethh.linkora.utils.Result
import kotlinx.serialization.encodeToString
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
                    response = NewItemResponseDTO(
                        message = "Folder created successfully with id = $it",
                        id = it,
                        correlation = addFolderDTO.correlation
                    ),
                    webSocketEvent = WebSocketEvent(
                        operation = FolderRoute.CREATE_FOLDER.name, payload = Json.encodeToJsonElement(
                            FolderDTO(
                                id = it,
                                name = addFolderDTO.name,
                                note = addFolderDTO.note,
                                parentFolderId = addFolderDTO.parentFolderId,
                                isArchived = addFolderDTO.isArchived,
                                correlation = addFolderDTO.correlation
                            )
                        ),
                    )
                )
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun deleteFolder(idBasedDTO: IDBasedDTO): Result<Message> {
        val folderId = idBasedDTO.id
        return try {
            transaction {
                FoldersTable.deleteWhere {
                    FoldersTable.id.eq(folderId)
                }
                TombStoneHelper.insert(payload = Json.encodeToString(idBasedDTO), operation = FolderRoute.DELETE_FOLDER.name)
            }
            when (val childFolders = getChildFolders(idBasedDTO)) {
                is Result.Failure -> {
                    throw childFolders.exception
                }

                is Result.Success -> {
                    childFolders.response.map { it.id }.forEach { childFolderId ->
                        deleteFolder(idBasedDTO.copy(id = childFolderId))
                    }
                }
            }
            Result.Success(
                response = "Folder and its contents have been successfully deleted.", webSocketEvent = WebSocketEvent(
                    operation = FolderRoute.DELETE_FOLDER.name,
                    payload = Json.encodeToJsonElement(idBasedDTO),
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun getChildFolders(idBasedDTO: IDBasedDTO): Result<List<Folder>> {
        return try {
            transaction {
                FoldersTable.selectAll().where {
                    FoldersTable.parentFolderID.eq(idBasedDTO.id)
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

    override suspend fun markAsArchive(idBasedDTO: IDBasedDTO): Result<Message> {
        val folderId = idBasedDTO.id
        return try {
            transaction {
                FoldersTable.update(where = { FoldersTable.id.eq(folderId) }) {
                    it[lastModified] = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                    it[isFolderArchived] = true
                }
            }.let {
                Result.Success(
                    response = "Number of rows affected by the update = $it", webSocketEvent = WebSocketEvent(
                        operation = FolderRoute.MARK_FOLDER_AS_ARCHIVE.name,
                        payload = Json.encodeToJsonElement(idBasedDTO),
                    )
                )
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun markAsRegularFolder(idBasedDTO: IDBasedDTO): Result<Message> {
        val folderId = idBasedDTO.id
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
                        payload = Json.encodeToJsonElement(idBasedDTO),
                    )
                )
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun changeParentFolder(changeParentFolderDTO: ChangeParentFolderDTO): Result<Message> {
        val folderId = changeParentFolderDTO.folderId
        val newParentFolderId = changeParentFolderDTO.newParentFolderId
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
                        payload = Json.encodeToJsonElement(changeParentFolderDTO),
                    )
                )
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun updateFolderName(updateFolderNameDTO: UpdateFolderNameDTO): Result<Message> {
        val folderId = updateFolderNameDTO.folderId
        val newFolderName = updateFolderNameDTO.newFolderName
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
                        payload = Json.encodeToJsonElement(updateFolderNameDTO),
                    )
                )
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun updateFolderNote(updateFolderNoteDTO: UpdateFolderNoteDTO): Result<Message> {
        val folderId = updateFolderNoteDTO.folderId
        val newNote = updateFolderNoteDTO.newNote
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
                        payload = Json.encodeToJsonElement(updateFolderNoteDTO),
                    )
                )
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun deleteFolderNote(idBasedDTO: IDBasedDTO): Result<Message> {
        return try {
            transaction {
                FoldersTable.update(where = { FoldersTable.id.eq(idBasedDTO.id) }) {
                    it[infoForSaving] = ""
                    it[lastModified] = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                }
            }.let {
                Result.Success(
                    response = "Number of rows affected by the update = $it", webSocketEvent = WebSocketEvent(
                        operation = FolderRoute.DELETE_FOLDER_NOTE.name,
                        payload = Json.encodeToJsonElement(idBasedDTO),
                    )
                )
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
}