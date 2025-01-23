package com.sakethh.linkora.domain.dto.folder

import com.sakethh.linkora.domain.dto.Correlation
import kotlinx.serialization.Serializable

@Serializable
data class AddFolderDTO(
    val name: String,
    val note: String,
    val parentFolderId: Long?,
    val isArchived: Boolean,
    val correlation: Correlation
)
