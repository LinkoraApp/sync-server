package com.sakethh.linkora.domain.dto.link

import com.sakethh.linkora.domain.dto.Correlation
import kotlinx.serialization.Serializable

@Serializable
data class DeleteDuplicateLinksDTO(
    val linkIds: List<Long>, val correlation: Correlation, val eventTimestamp: Long
)
