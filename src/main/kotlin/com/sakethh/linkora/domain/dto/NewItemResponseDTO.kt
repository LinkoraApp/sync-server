package com.sakethh.linkora.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class NewItemResponseDTO(
    val message: String,
    val id: Long,
    val correlation: Correlation
)