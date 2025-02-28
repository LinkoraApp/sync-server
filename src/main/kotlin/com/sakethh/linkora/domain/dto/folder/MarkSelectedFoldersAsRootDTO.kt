package com.sakethh.linkora.domain.dto.folder

import com.sakethh.linkora.domain.dto.Correlation
import kotlinx.serialization.Serializable

@Serializable
data class MarkSelectedFoldersAsRootDTO(
    val folderIds: List<Long>,
    val eventTimestamp: Long,
    val correlation: Correlation
)
