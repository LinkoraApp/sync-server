package com.sakethh.linkora.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateFolderNoteDTO(
    val folderId: Long,
    val newNote: String
)
