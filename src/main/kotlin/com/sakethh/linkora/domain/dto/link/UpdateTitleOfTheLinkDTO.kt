package com.sakethh.linkora.domain.dto.link

import com.sakethh.linkora.domain.LinkType

data class UpdateTitleOfTheLinkDTO(
    val linkType: LinkType,
    val linkId: Long,
    val newTitleOfTheLink: String
)
