package com.sakethh.linkora.domain.dto.link

import kotlinx.serialization.Serializable

@Serializable
data class UpdateNoteOfALinkDTO(
    val linkId: Long,
    val newNote: String,
    val correlationId:String
)
