package com.sakethh.linkora.domain.dto.link

import com.sakethh.linkora.domain.dto.Correlation
import kotlinx.serialization.Serializable

@Serializable
data class UpdateTitleOfTheLinkDTO(
    val linkId: Long,
    val newTitleOfTheLink: String,
    val correlation: Correlation
)
