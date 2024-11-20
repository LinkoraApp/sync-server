package com.sakethh.linkora.domain.dto.link

import com.sakethh.linkora.domain.LinkType
import kotlinx.serialization.Serializable

@Serializable
data class DeleteALinkDTO(
    val linkType: LinkType,
    val linkId: Long
)
