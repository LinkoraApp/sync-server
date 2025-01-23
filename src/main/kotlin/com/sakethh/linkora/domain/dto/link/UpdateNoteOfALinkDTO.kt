package com.sakethh.linkora.domain.dto.link

import com.sakethh.linkora.domain.dto.Correlation
import kotlinx.serialization.Serializable

@Serializable
data class UpdateNoteOfALinkDTO(
    val linkId: Long,
    val newNote: String,
    val correlation: Correlation
)
