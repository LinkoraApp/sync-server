package com.sakethh.linkora.domain.dto.savedAndFolderLinks

import kotlinx.serialization.Serializable

@Serializable
data class UpdateNoteOfALinkDTO(
    val linkId: Long,
    val newNote: String
)
