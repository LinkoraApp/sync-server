package com.sakethh.linkora.domain.dto.tag

import com.sakethh.linkora.domain.dto.Correlation
import kotlinx.serialization.Serializable

@Serializable
data class CreateTagDTO(
    val name: String,
    val eventTimestamp: Long,
    val correlation: Correlation
)