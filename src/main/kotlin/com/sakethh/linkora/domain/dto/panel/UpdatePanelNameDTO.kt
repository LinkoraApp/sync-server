package com.sakethh.linkora.domain.dto.panel

import kotlinx.serialization.Serializable

@Serializable
data class UpdatePanelNameDTO(
    val newName: String, val panelId: Long,
    val correlationId:String
)
