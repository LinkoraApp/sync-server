package com.sakethh.linkora.domain.dto.panel

import com.sakethh.linkora.domain.dto.Correlation
import kotlinx.serialization.Serializable

@Serializable
data class PanelFolderDTO(
    val id: Long,
    val folderId: Long,
    val panelPosition: Long,
    val folderName: String,
    val connectedPanelId: Long,
    val correlation: Correlation
)
