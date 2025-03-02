package com.sakethh.linkora.data.repository

import com.sakethh.linkora.domain.LWWConflictException
import com.sakethh.linkora.domain.dto.IDBasedDTO
import com.sakethh.linkora.domain.dto.NewItemResponseDTO
import com.sakethh.linkora.domain.dto.TimeStampBasedResponse
import com.sakethh.linkora.domain.dto.panel.*
import com.sakethh.linkora.domain.model.WebSocketEvent
import com.sakethh.linkora.domain.repository.PanelsRepository
import com.sakethh.linkora.domain.routes.PanelRoute
import com.sakethh.linkora.domain.tables.PanelFoldersTable
import com.sakethh.linkora.domain.tables.PanelsTable
import com.sakethh.linkora.domain.tables.helper.TombStoneHelper
import com.sakethh.linkora.domain.Result
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant

class PanelsRepoImpl : PanelsRepository {
    private fun checkForPanelsLWWConflictAndThrow(id: Long, timeStamp: Long) {
        transaction {
            PanelsTable.select(PanelsTable.lastModified).where {
                PanelsTable.id.eq(id)
            }.let {
                if (it.single()[PanelsTable.lastModified] > timeStamp) {
                    throw LWWConflictException()
                }
            }
        }
    }

    override suspend fun addANewPanel(addANewPanelDTO: AddANewPanelDTO): Result<NewItemResponseDTO> {
        return try {
            val eventTimestamp = Instant.now().epochSecond
            transaction {
                PanelsTable.insertAndGetId {
                    it[panelName] = addANewPanelDTO.panelName
                    it[lastModified] = eventTimestamp
                }
            }.value.let {
                Result.Success(
                    response = NewItemResponseDTO(
                        timeStampBasedResponse = TimeStampBasedResponse(
                            message = "New panel added with id : $it",
                            eventTimestamp = eventTimestamp
                        ),
                        id = it,
                        correlation = addANewPanelDTO.correlation,
                    ), webSocketEvent = WebSocketEvent(
                        operation = PanelRoute.ADD_A_NEW_PANEL.name, payload = Json.encodeToJsonElement(
                            PanelDTO(
                                panelId = it,
                                panelName = addANewPanelDTO.panelName,
                                correlation = addANewPanelDTO.correlation,
                                eventTimestamp = eventTimestamp
                            )
                        )
                    )
                )
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun addANewFolderInAPanel(addANewPanelFolderDTO: AddANewPanelFolderDTO): Result<NewItemResponseDTO> {
        return try {
            val eventTimestamp = Instant.now().epochSecond
            transaction {
                PanelFoldersTable.insertAndGetId {
                    it[folderId] = addANewPanelFolderDTO.folderId
                    it[folderName] = addANewPanelFolderDTO.folderName
                    it[panelPosition] = addANewPanelFolderDTO.panelPosition
                    it[connectedPanelId] = addANewPanelFolderDTO.connectedPanelId
                    it[lastModified] = eventTimestamp
                }
            }.value.let {
                Result.Success(
                    response = NewItemResponseDTO(
                        timeStampBasedResponse = TimeStampBasedResponse(
                            message = "New folder added in a panel (id : ${addANewPanelFolderDTO.connectedPanelId}) with id : $it",
                            eventTimestamp = eventTimestamp
                        ),
                        id = it,
                        correlation = addANewPanelFolderDTO.correlation,
                    ), webSocketEvent = WebSocketEvent(
                        operation = PanelRoute.ADD_A_NEW_FOLDER_IN_A_PANEL.name, payload = Json.encodeToJsonElement(
                            PanelFolderDTO(
                                id = it,
                                folderId = addANewPanelFolderDTO.folderId,
                                panelPosition = addANewPanelFolderDTO.panelPosition,
                                folderName = addANewPanelFolderDTO.folderName,
                                connectedPanelId = addANewPanelFolderDTO.connectedPanelId,
                                correlation = addANewPanelFolderDTO.correlation,
                                eventTimestamp = eventTimestamp
                            )
                        )
                    )
                )
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun deleteAPanel(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse> {
        return try {
            val eventTimestamp = Instant.now().epochSecond
            transaction {
                PanelsTable.deleteWhere {
                    PanelsTable.id.eq(idBasedDTO.id)
                }
                PanelFoldersTable.deleteWhere {
                    connectedPanelId.eq(idBasedDTO.id)
                }
                TombStoneHelper.insert(
                    payload = Json.encodeToString(idBasedDTO),
                    operation = PanelRoute.DELETE_A_PANEL.name,
                    eventTimestamp
                )
            }
            Result.Success(
                response = TimeStampBasedResponse(
                    message = "Deleted the panel and respective connected panel folders (id : ${idBasedDTO.id}) successfully.",
                    eventTimestamp = eventTimestamp
                ),
                webSocketEvent = WebSocketEvent(
                    operation = PanelRoute.DELETE_A_PANEL.name,
                    payload = Json.encodeToJsonElement(idBasedDTO.copy(eventTimestamp = eventTimestamp))
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun updateAPanelName(updatePanelNameDTO: UpdatePanelNameDTO): Result<TimeStampBasedResponse> {
        return try {
            checkForPanelsLWWConflictAndThrow(updatePanelNameDTO.panelId, updatePanelNameDTO.eventTimestamp)
            val eventTimestamp = Instant.now().epochSecond
            transaction {
                PanelsTable.update(where = {
                    PanelsTable.id.eq(updatePanelNameDTO.panelId)
                }) {
                    it[panelName] = updatePanelNameDTO.newName
                    it[lastModified] = eventTimestamp
                }
            }
            Result.Success(
                response = TimeStampBasedResponse(
                    eventTimestamp = eventTimestamp,
                    message = "Updated panel name to ${updatePanelNameDTO.newName} (id : ${updatePanelNameDTO.panelId})."
                ),
                webSocketEvent = WebSocketEvent(
                    operation = PanelRoute.UPDATE_A_PANEL_NAME.name,
                    payload = Json.encodeToJsonElement(updatePanelNameDTO.copy(eventTimestamp = eventTimestamp))
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun deleteAFolderFromAllPanels(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse> {
        return try {
            val eventTimestamp = Instant.now().epochSecond
            transaction {
                PanelFoldersTable.deleteWhere {
                    folderId.eq(idBasedDTO.id)
                }
                TombStoneHelper.insert(
                    payload = Json.encodeToString(idBasedDTO.copy(eventTimestamp = eventTimestamp)),
                    operation = PanelRoute.DELETE_A_FOLDER_FROM_ALL_PANELS.name,
                    eventTimestamp
                )
            }
            Result.Success(
                response = TimeStampBasedResponse(
                    eventTimestamp = eventTimestamp,
                    message = "Deleted folder from all panel folders where id = ${idBasedDTO.id}."
                ),
                webSocketEvent = WebSocketEvent(
                    operation = PanelRoute.DELETE_A_FOLDER_FROM_ALL_PANELS.name,
                    payload = Json.encodeToJsonElement(
                        idBasedDTO.copy(
                            eventTimestamp = eventTimestamp
                        )
                    )
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun deleteAFolderFromAPanel(deleteAPanelFromAFolderDTO: DeleteAFolderFromAPanelDTO): Result<TimeStampBasedResponse> {
        return try {
            val eventTimestamp = Instant.now().epochSecond
            transaction {
                PanelFoldersTable.deleteWhere {
                    folderId.eq(deleteAPanelFromAFolderDTO.folderID) and connectedPanelId.eq(deleteAPanelFromAFolderDTO.panelId)
                }
                TombStoneHelper.insert(
                    payload = Json.encodeToString(deleteAPanelFromAFolderDTO.copy(eventTimestamp = eventTimestamp)),
                    operation = PanelRoute.DELETE_A_FOLDER_FROM_A_PANEL.name,
                    eventTimestamp
                )
            }
            Result.Success(
                response = TimeStampBasedResponse(
                    eventTimestamp = eventTimestamp,
                    message = "Deleted the folder with id ${deleteAPanelFromAFolderDTO.folderID} from a panel with id ${deleteAPanelFromAFolderDTO.panelId}."
                ),
                webSocketEvent = WebSocketEvent(
                    operation = PanelRoute.DELETE_A_FOLDER_FROM_A_PANEL.name,
                    payload = Json.encodeToJsonElement(deleteAPanelFromAFolderDTO.copy(eventTimestamp = eventTimestamp))
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun deleteAllFoldersFromAPanel(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse> {
        return try {
            val eventTimestamp = Instant.now().epochSecond
            transaction {
                PanelFoldersTable.deleteWhere {
                    connectedPanelId.eq(idBasedDTO.id)
                }
                TombStoneHelper.insert(
                    deletedAt = eventTimestamp,
                    payload = Json.encodeToString(idBasedDTO.copy(eventTimestamp = eventTimestamp)),
                    operation = PanelRoute.DELETE_ALL_FOLDERS_FROM_A_PANEL.name
                )
            }
            Result.Success(
                response = TimeStampBasedResponse(
                    message = "Deleted all folders from the panel with id : ${idBasedDTO.id}.",
                    eventTimestamp = eventTimestamp
                ),
                webSocketEvent = WebSocketEvent(
                    operation = PanelRoute.DELETE_ALL_FOLDERS_FROM_A_PANEL.name,
                    payload = Json.encodeToJsonElement(idBasedDTO.copy(eventTimestamp = eventTimestamp))
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
}