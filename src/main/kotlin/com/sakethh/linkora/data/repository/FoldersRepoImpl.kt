package com.sakethh.linkora.data.repository

import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.Route
import com.sakethh.linkora.domain.dto.IDBasedDTO
import com.sakethh.linkora.domain.dto.NewItemResponseDTO
import com.sakethh.linkora.domain.dto.TimeStampBasedResponse
import com.sakethh.linkora.domain.dto.folder.*
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.WebSocketEvent
import com.sakethh.linkora.domain.repository.FoldersRepo
import com.sakethh.linkora.domain.repository.PanelsRepo
import com.sakethh.linkora.domain.tables.FoldersTable
import com.sakethh.linkora.domain.tables.LinksTable
import com.sakethh.linkora.domain.tables.PanelFoldersTable
import com.sakethh.linkora.domain.tables.helper.TombStoneHelper
import com.sakethh.linkora.utils.checkForLWWConflictAndThrow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.Instant

class FoldersRepoImpl(private val panelsRepo: PanelsRepo) : FoldersRepo {

    override suspend fun createFolder(addFolderDTO: AddFolderDTO): Result<NewItemResponseDTO> {
        return try {
            val eventTimestamp = Instant.now().epochSecond
            transaction {
                FoldersTable.insertAndGetId { folder ->
                    folder[lastModified] = eventTimestamp
                    folder[folderName] = addFolderDTO.name
                    folder[note] = addFolderDTO.note
                    folder[parentFolderID] = addFolderDTO.parentFolderId
                    folder[isFolderArchived] = addFolderDTO.isArchived
                }
            }.value.let {
                Result.Success(
                    response = NewItemResponseDTO(
                        timeStampBasedResponse = TimeStampBasedResponse(
                            message = "Folder created successfully with id = $it", eventTimestamp = eventTimestamp
                        ), id = it, correlation = addFolderDTO.correlation
                    ), webSocketEvent = WebSocketEvent(
                        operation = Route.Folder.CREATE_FOLDER.name,
                        payload = Json.encodeToJsonElement(
                            FolderDTO(
                                id = it,
                                name = addFolderDTO.name,
                                note = addFolderDTO.note,
                                parentFolderId = addFolderDTO.parentFolderId,
                                isArchived = addFolderDTO.isArchived,
                                correlation = addFolderDTO.correlation,
                                eventTimestamp = eventTimestamp
                            )
                        ),
                    )
                )
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun updateFolder(folderDTO: FolderDTO): Result<TimeStampBasedResponse> {
        return try {
            FoldersTable.checkForLWWConflictAndThrow(
                id = folderDTO.id, timeStamp = folderDTO.eventTimestamp, lastModifiedColumn = FoldersTable.lastModified
            )
            val eventTimestamp = Instant.now().epochSecond
            transaction {
                FoldersTable.update(where = {
                    FoldersTable.id eq folderDTO.id
                }) { folder ->
                    folder[lastModified] = eventTimestamp
                    folder[folderName] = folderDTO.name
                    folder[note] = folderDTO.note
                    folder[parentFolderID] = folderDTO.parentFolderId
                    folder[isFolderArchived] = folderDTO.isArchived
                }
            }.let {
                Result.Success(
                    response = TimeStampBasedResponse(
                        message = "Folder updated successfully.", eventTimestamp = eventTimestamp
                    ), webSocketEvent = WebSocketEvent(
                        operation = Route.Folder.UPDATE_FOLDER.name,
                        payload = Json.encodeToJsonElement(folderDTO.copy(eventTimestamp = eventTimestamp)),
                    )
                )
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun deleteFolder(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse> {
        return try {
            val eventTimestamp = Instant.now().epochSecond
            when (val childFolders = getChildFolders(idBasedDTO)) {
                is Result.Failure -> {
                    throw childFolders.exception
                }

                is Result.Success -> {
                    childFolders.response.map { it.id }.forEach { childFolderId ->
                        panelsRepo.deleteAFolderFromAllPanels(
                            IDBasedDTO(
                                id = childFolderId,
                                correlation = idBasedDTO.correlation,
                                eventTimestamp = idBasedDTO.eventTimestamp
                            )
                        )
                        transaction {
                            FoldersTable.deleteWhere {
                                FoldersTable.id.eq(childFolderId)
                            }
                            LinksTable.deleteWhere {
                                idOfLinkedFolder.eq(childFolderId)
                            }
                        }
                        deleteFolder(idBasedDTO.copy(id = childFolderId, eventTimestamp = eventTimestamp))
                    }
                }
            }
            transaction {
                FoldersTable.deleteWhere {
                    FoldersTable.id.eq(idBasedDTO.id)
                }
                LinksTable.deleteWhere {
                    idOfLinkedFolder.eq(idBasedDTO.id)
                }
                TombStoneHelper.insert(
                    payload = Json.encodeToString(idBasedDTO.copy(eventTimestamp = eventTimestamp)),
                    operation = Route.Folder.DELETE_FOLDER.name,
                    eventTimestamp
                )
            }
            Result.Success(
                response = TimeStampBasedResponse(
                    message = "Folder and its contents have been successfully deleted.", eventTimestamp = eventTimestamp
                ), webSocketEvent = WebSocketEvent(
                    operation = Route.Folder.DELETE_FOLDER.name,
                    payload = Json.encodeToJsonElement(idBasedDTO.copy(eventTimestamp = eventTimestamp)),
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
                        note = it[FoldersTable.note],
                        parentFolderId = it[FoldersTable.parentFolderID],
                        isArchived = it[FoldersTable.isFolderArchived],
                        eventTimestamp = it[FoldersTable.lastModified]
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
                        note = it[FoldersTable.note],
                        parentFolderId = it[FoldersTable.parentFolderID],
                        isArchived = it[FoldersTable.isFolderArchived],
                        eventTimestamp = it[FoldersTable.lastModified]
                    )
                }
            }.let {
                Result.Success(response = it, webSocketEvent = null)
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun markAsArchive(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse> {
        val folderId = idBasedDTO.id
        return try {
            FoldersTable.checkForLWWConflictAndThrow(
                id = idBasedDTO.id,
                timeStamp = idBasedDTO.eventTimestamp,
                lastModifiedColumn = FoldersTable.lastModified
            )
            val eventTimestamp = Instant.now().epochSecond
            transaction {
                FoldersTable.update(where = { FoldersTable.id.eq(folderId) }) {
                    it[lastModified] = eventTimestamp
                    it[isFolderArchived] = true
                }
            }.let {
                Result.Success(
                    response = TimeStampBasedResponse(
                        eventTimestamp = eventTimestamp, message = "Number of rows affected by the update = $it"
                    ), webSocketEvent = WebSocketEvent(
                        operation = Route.Folder.MARK_FOLDER_AS_ARCHIVE.name,
                        payload = Json.encodeToJsonElement(idBasedDTO.copy(eventTimestamp = eventTimestamp)),
                    )
                )
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun markAsRegularFolder(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse> {
        val folderId = idBasedDTO.id
        return try {
            FoldersTable.checkForLWWConflictAndThrow(
                id = idBasedDTO.id,
                timeStamp = idBasedDTO.eventTimestamp,
                lastModifiedColumn = FoldersTable.lastModified
            )
            val eventTimestamp = Instant.now().epochSecond
            transaction {
                FoldersTable.update(where = { FoldersTable.id.eq(folderId) }) {
                    it[lastModified] = eventTimestamp
                    it[isFolderArchived] = false
                }
            }.let {
                Result.Success(
                    response = TimeStampBasedResponse(
                        eventTimestamp = eventTimestamp, message = "Number of rows affected by the update = $it"
                    ), webSocketEvent = WebSocketEvent(
                        operation = Route.Folder.MARK_AS_REGULAR_FOLDER.name,
                        payload = Json.encodeToJsonElement(idBasedDTO.copy(eventTimestamp = eventTimestamp)),
                    )
                )
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun updateFolderName(updateFolderNameDTO: UpdateFolderNameDTO): Result<TimeStampBasedResponse> {
        val folderId = updateFolderNameDTO.folderId
        val newFolderName = updateFolderNameDTO.newFolderName
        return try {
            FoldersTable.checkForLWWConflictAndThrow(
                id = updateFolderNameDTO.folderId,
                timeStamp = updateFolderNameDTO.eventTimestamp,
                lastModifiedColumn = FoldersTable.lastModified
            )
            val eventTimestamp = Instant.now().epochSecond
            transaction {
                FoldersTable.update(where = { FoldersTable.id.eq(folderId) }) {
                    it[lastModified] = eventTimestamp
                    it[folderName] = newFolderName
                }
                PanelFoldersTable.update(where = {
                    PanelFoldersTable.folderId.eq(folderId)
                }) {
                    it[lastModified] = eventTimestamp
                    it[folderName] = newFolderName
                }
            }.let {
                Result.Success(
                    response = TimeStampBasedResponse(
                        eventTimestamp = eventTimestamp, message = "Number of rows affected by the update = $it"
                    ), webSocketEvent = WebSocketEvent(
                        operation = Route.Folder.UPDATE_FOLDER_NAME.name,
                        payload = Json.encodeToJsonElement(updateFolderNameDTO.copy(eventTimestamp = eventTimestamp)),
                    )
                )
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun updateFolderNote(updateFolderNoteDTO: UpdateFolderNoteDTO): Result<TimeStampBasedResponse> {
        val folderId = updateFolderNoteDTO.folderId
        val newNote = updateFolderNoteDTO.newNote
        return try {
            FoldersTable.checkForLWWConflictAndThrow(
                id = updateFolderNoteDTO.folderId,
                timeStamp = updateFolderNoteDTO.eventTimestamp,
                lastModifiedColumn = FoldersTable.lastModified
            )
            val eventTimestamp = Instant.now().epochSecond
            transaction {
                FoldersTable.update(where = { FoldersTable.id.eq(folderId) }) {
                    it[lastModified] = eventTimestamp
                    it[note] = newNote
                }
            }.let {
                Result.Success(
                    response = TimeStampBasedResponse(
                        message = "Number of rows affected by the update = $it", eventTimestamp = eventTimestamp
                    ), webSocketEvent = WebSocketEvent(
                        operation = Route.Folder.UPDATE_FOLDER_NOTE.name,
                        payload = Json.encodeToJsonElement(updateFolderNoteDTO.copy(eventTimestamp = eventTimestamp)),
                    )
                )
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun deleteFolderNote(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse> {
        return try {
            val eventTimestamp = Instant.now().epochSecond
            transaction {
                FoldersTable.update(where = { FoldersTable.id.eq(idBasedDTO.id) }) {
                    it[note] = ""
                    it[lastModified] = eventTimestamp
                }
            }.let {
                Result.Success(
                    response = TimeStampBasedResponse(
                        eventTimestamp = eventTimestamp, message = "Number of rows affected by the update = $it"
                    ), webSocketEvent = WebSocketEvent(
                        operation = Route.Folder.DELETE_FOLDER_NOTE.name,
                        payload = Json.encodeToJsonElement(idBasedDTO.copy(eventTimestamp = eventTimestamp)),
                    )
                )
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun markSelectedFoldersAsRoot(markSelectedFoldersAsRootDTO: MarkSelectedFoldersAsRootDTO): Result<TimeStampBasedResponse> {
        return try {
            FoldersTable.checkForLWWConflictAndThrow(
                id = markSelectedFoldersAsRootDTO.folderIds.last(),
                timeStamp = markSelectedFoldersAsRootDTO.eventTimestamp,
                lastModifiedColumn = FoldersTable.lastModified
            )
            val eventTimeStamp = Instant.now().epochSecond
            transaction {
                FoldersTable.update(where = {
                    FoldersTable.id.inList(markSelectedFoldersAsRootDTO.folderIds)
                }) {
                    it[lastModified] = eventTimeStamp
                    it[parentFolderID] = null
                }
            }.let {
                Result.Success(
                    response = TimeStampBasedResponse(
                        eventTimestamp = eventTimeStamp, message = "Marked $it folders as root."
                    ), webSocketEvent = WebSocketEvent(
                        operation = Route.Folder.MARK_FOLDERS_AS_ROOT.name,
                        payload = Json.encodeToJsonElement(markSelectedFoldersAsRootDTO.copy(eventTimestamp = eventTimeStamp))
                    )
                )
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
}