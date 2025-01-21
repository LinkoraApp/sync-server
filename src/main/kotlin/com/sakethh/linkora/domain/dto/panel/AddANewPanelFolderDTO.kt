package com.sakethh.linkora.domain.dto.panel

import kotlinx.serialization.Serializable

@Serializable
data class AddANewPanelFolderDTO(
    val folderId: Long,
    val panelPosition: Long,
    val folderName: String,
    val connectedPanelId: Long
)