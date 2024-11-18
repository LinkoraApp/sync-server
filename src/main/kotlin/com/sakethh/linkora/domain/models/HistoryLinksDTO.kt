package com.sakethh.linkora.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class HistoryLinksDTO(
    val id: Long,
    val linkTitle: String,
    val webURL: String,
    val baseURL: String,
    val imgURL: String,
    val infoForSaving: String,
    val userAgent: String?
)
