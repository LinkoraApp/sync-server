package com.sakethh.linkora.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class CopyItemsResponseDTO(
    val folders: List<CopiedFolderResponse>,
    val linkIds: Map<Long, Long>,
    val correlation: Correlation,
    val eventTimestamp: Long
)

@Serializable
data class CopiedFolderResponse(
    val currentFolder: CurrentFolder, val links: List<FolderLink>
)