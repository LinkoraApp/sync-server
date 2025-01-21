package com.sakethh.linkora.domain.dto.panel

import kotlinx.serialization.Serializable

@Serializable
data class DeleteAPanelFromAFolderDTO(
    val panelId: Long,
    val folderID: Long
)
