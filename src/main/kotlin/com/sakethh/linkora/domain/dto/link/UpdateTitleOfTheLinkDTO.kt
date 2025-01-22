package com.sakethh.linkora.domain.dto.link

import kotlinx.serialization.Serializable

@Serializable
data class UpdateTitleOfTheLinkDTO(
    val linkId: Long,
    val newTitleOfTheLink: String,
    val correlationId:String
)
