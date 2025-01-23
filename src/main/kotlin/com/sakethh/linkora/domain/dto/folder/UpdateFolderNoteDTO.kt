package com.sakethh.linkora.domain.dto.folder

import com.sakethh.linkora.domain.dto.Correlation
import kotlinx.serialization.Serializable

@Serializable
data class UpdateFolderNoteDTO(
    val folderId: Long,
    val newNote: String,
    val correlation: Correlation
)
