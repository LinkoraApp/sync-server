package com.sakethh.linkora.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LinkTag(
    val linkId: Long,
    val tagId: Long,
    val eventTimestamp: Long
)
