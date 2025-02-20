package com.sakethh.linkora.domain.dto.panel

import com.sakethh.linkora.domain.dto.Correlation
import kotlinx.serialization.Serializable

@Serializable
data class DeleteAFolderFromAPanelDTO(
    val panelId: Long,
    val folderID: Long,
    val correlation: Correlation,
    val eventTimestamp: Long
)
