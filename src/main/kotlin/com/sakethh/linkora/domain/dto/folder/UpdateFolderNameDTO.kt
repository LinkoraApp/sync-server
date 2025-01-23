package com.sakethh.linkora.domain.dto.folder

import com.sakethh.linkora.domain.dto.Correlation
import kotlinx.serialization.Serializable

@Serializable
data class UpdateFolderNameDTO(
    val folderId: Long,
    val newFolderName: String,
    val correlation: Correlation
)
