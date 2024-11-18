package com.sakethh.linkora.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class ArchiveLinkDTO(
    val id: Long,
    val linkTitle: String,
    val webURL: String,
    val baseURL: String,
    val imgURL: String,
    val infoForSaving: String,
    val userAgent: String?
)
