package com.sakethh.linkora.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class NewItemResponseDTO(
    val timeStampBasedResponse: TimeStampBasedResponse,
    val id: Long,
    val correlation: Correlation
)