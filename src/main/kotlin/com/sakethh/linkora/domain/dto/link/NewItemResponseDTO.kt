package com.sakethh.linkora.domain.dto.link

import kotlinx.serialization.Serializable

@Serializable
data class NewItemResponseDTO(
    val message: String,
    val id: Long
)