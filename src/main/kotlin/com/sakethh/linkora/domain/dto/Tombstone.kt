package com.sakethh.linkora.domain.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Tombstone(
    val deletedAt: String, val payload: JsonElement
)
