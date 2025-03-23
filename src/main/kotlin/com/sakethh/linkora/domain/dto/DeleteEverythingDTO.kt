package com.sakethh.linkora.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class DeleteEverythingDTO(
    val eventTimestamp: Long, val correlation: Correlation
)
