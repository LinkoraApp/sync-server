package com.sakethh.linkora.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    val id:Long,
    val name: String,
    val eventTimestamp: Long
)
