package com.sakethh.linkora.domain.dto.panel

import com.sakethh.linkora.domain.dto.Correlation
import kotlinx.serialization.Serializable

@Serializable
data class PanelDTO(
    val panelId:Long,
    val panelName:String,
    val correlation: Correlation,
    val eventTimeStamp: Long
)
