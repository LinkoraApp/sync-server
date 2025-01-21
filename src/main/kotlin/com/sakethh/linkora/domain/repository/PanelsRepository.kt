package com.sakethh.linkora.domain.repository

import com.sakethh.linkora.domain.dto.link.NewItemResponseDTO
import com.sakethh.linkora.domain.dto.panel.AddANewPanelDTO
import com.sakethh.linkora.domain.dto.panel.AddANewPanelFolderDTO
import com.sakethh.linkora.domain.dto.panel.DeleteAPanelFromAFolderDTO
import com.sakethh.linkora.domain.dto.panel.UpdatePanelNameDTO
import com.sakethh.linkora.utils.Result

interface PanelsRepository {
    suspend fun addANewPanel(addANewPanelDTO: AddANewPanelDTO): Result<NewItemResponseDTO>
    suspend fun addANewFolderInAPanel(addANewPanelFolderDTO: AddANewPanelFolderDTO): Result<NewItemResponseDTO>
    suspend fun deleteAPanel(id: Long):Result<Message>
    suspend fun updateAPanelName(updatePanelNameDTO: UpdatePanelNameDTO):Result<Message>
    suspend fun deleteAFolderFromAllPanels(folderID: Long):Result<Message>
    suspend fun deleteAFolderFromAPanel(deleteAPanelFromAFolderDTO: DeleteAPanelFromAFolderDTO):Result<Message>
}