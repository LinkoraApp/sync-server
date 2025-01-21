package com.sakethh.linkora.data.repository

import com.sakethh.linkora.domain.dto.link.NewItemResponseDTO
import com.sakethh.linkora.domain.dto.panel.AddANewPanelDTO
import com.sakethh.linkora.domain.dto.panel.AddANewPanelFolderDTO
import com.sakethh.linkora.domain.dto.panel.DeleteAPanelFromAFolderDTO
import com.sakethh.linkora.domain.dto.panel.UpdatePanelNameDTO
import com.sakethh.linkora.domain.repository.Message
import com.sakethh.linkora.domain.repository.PanelsRepository
import com.sakethh.linkora.domain.tables.PanelFoldersTable
import com.sakethh.linkora.domain.tables.PanelsTable
import com.sakethh.linkora.utils.Result
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class PanelsRepoImpl : PanelsRepository {
    override suspend fun addANewPanel(addANewPanelDTO: AddANewPanelDTO): Result<NewItemResponseDTO> {
        return try {
            transaction {
                PanelsTable.insertAndGetId {
                    it[panelName] = addANewPanelDTO.panelName
                }
            }.value.let {
                Result.Success(
                    NewItemResponseDTO(
                        message = "New panel added with id : $it", id = it
                    )
                )
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun addANewFolderInAPanel(addANewPanelFolderDTO: AddANewPanelFolderDTO): Result<NewItemResponseDTO> {
        return try {
            transaction {
                PanelFoldersTable.insertAndGetId {
                    it[folderId] = addANewPanelFolderDTO.folderId
                    it[folderName] = addANewPanelFolderDTO.folderName
                    it[panelPosition] = addANewPanelFolderDTO.panelPosition
                    it[connectedPanelId] = addANewPanelFolderDTO.connectedPanelId
                }
            }.value.let {
                Result.Success(
                    NewItemResponseDTO(
                        message = "New folder added in a panel (id : ${addANewPanelFolderDTO.connectedPanelId}) with id : $it",
                        id = it
                    )
                )
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun deleteAPanel(id: Long): Result<Message> {
        return try {
            transaction {
                PanelsTable.deleteWhere {
                    PanelsTable.id.eq(id)
                }
                PanelFoldersTable.deleteWhere {
                    connectedPanelId.eq(id)
                }
            }
            Result.Success("Deleted the panel and respective connected panel folders (id : $id) successfully.")
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun updateAPanelName(updatePanelNameDTO: UpdatePanelNameDTO): Result<Message> {
        return try {
            transaction {
                PanelsTable.update(where = {
                    PanelsTable.id.eq(updatePanelNameDTO.panelId)
                }) {
                    it[panelName] = updatePanelNameDTO.newName
                }
            }
            Result.Success("Updated panel name to ${updatePanelNameDTO.newName} (id : ${updatePanelNameDTO.panelId}).")
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun deleteAFolderFromAllPanels(folderID: Long): Result<Message> {
        return try {
            transaction {
                PanelFoldersTable.deleteWhere {
                    folderId.eq(folderID)
                }
            }
            Result.Success("Deleted folder from all panel folders where id = $folderID.")
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun deleteAFolderFromAPanel(deleteAPanelFromAFolderDTO: DeleteAPanelFromAFolderDTO): Result<Message> {
        return try {
            transaction {
                PanelFoldersTable.deleteWhere {
                    folderId.eq(deleteAPanelFromAFolderDTO.folderID) and connectedPanelId.eq(deleteAPanelFromAFolderDTO.panelId)
                }
            }
            Result.Success("Deleted the folder with id ${deleteAPanelFromAFolderDTO.folderID} from a panel with id ${deleteAPanelFromAFolderDTO.panelId}.")
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
}