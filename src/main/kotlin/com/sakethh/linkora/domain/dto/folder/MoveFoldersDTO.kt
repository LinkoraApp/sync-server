package com.sakethh.linkora.domain.dto.folder

import com.sakethh.linkora.domain.dto.Correlation
import kotlinx.serialization.Serializable

@Serializable
data class MoveFoldersDTO(
    val folderIds: List<Long>,
    val newParentFolderId: Long,
    val correlation: Correlation,
    val eventTimestamp: Long
)
