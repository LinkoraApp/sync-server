package com.sakethh.linkora.domain.repository

import com.sakethh.linkora.domain.dto.IDBasedDTO
import com.sakethh.linkora.domain.dto.NewItemResponseDTO
import com.sakethh.linkora.domain.dto.TimeStampBasedResponse
import com.sakethh.linkora.domain.dto.panel.AddANewPanelDTO
import com.sakethh.linkora.domain.dto.panel.AddANewPanelFolderDTO
import com.sakethh.linkora.domain.dto.panel.DeleteAFolderFromAPanelDTO
import com.sakethh.linkora.domain.dto.panel.UpdatePanelNameDTO
import com.sakethh.linkora.domain.Result

interface PanelsRepo {
    suspend fun addANewPanel(addANewPanelDTO: AddANewPanelDTO): Result<NewItemResponseDTO>
    suspend fun addANewFolderInAPanel(addANewPanelFolderDTO: AddANewPanelFolderDTO): Result<NewItemResponseDTO>
    suspend fun deleteAPanel(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse>
    suspend fun updateAPanelName(updatePanelNameDTO: UpdatePanelNameDTO): Result<TimeStampBasedResponse>
    suspend fun deleteAFolderFromAllPanels(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse>
    suspend fun deleteAFolderFromAPanel(deleteAPanelFromAFolderDTO: DeleteAFolderFromAPanelDTO): Result<TimeStampBasedResponse>
    suspend fun deleteAllFoldersFromAPanel(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse>
}