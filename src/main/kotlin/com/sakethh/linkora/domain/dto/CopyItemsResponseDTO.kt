package com.sakethh.linkora.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class CopyItemsResponseDTO(
    val folders: List<CopyFolderDTO>,
    val linkIds: Map<Long, Long>,
    val correlation: Correlation,
    val eventTimestamp: Long
)
