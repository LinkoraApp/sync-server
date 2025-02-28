package com.sakethh.linkora.domain.dto.link

import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.dto.Correlation
import kotlinx.serialization.Serializable

@Serializable
data class MoveLinksDTO(
    val linkIds: List<Long>,
    val parentFolderId: Long,
    val linkType: LinkType,
    val correlation: Correlation,
    val eventTimestamp: Long
)
