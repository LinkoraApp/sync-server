package com.sakethh.linkora.domain.dto.panel

import kotlinx.serialization.Serializable

@Serializable
data class PanelDTO(
    val panelId:Long,
    val panelName:String,
    val correlationId:String
)
