package com.sakethh.linkora.domain

import kotlinx.serialization.Serializable

@Serializable
data class Panel(
    val panelId: Long,
    val panelName: String,
)
