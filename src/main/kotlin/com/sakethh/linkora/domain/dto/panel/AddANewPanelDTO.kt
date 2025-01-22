package com.sakethh.linkora.domain.dto.panel

import kotlinx.serialization.Serializable

@Serializable
data class AddANewPanelDTO(
    val panelName: String,
    val correlationId:String
)