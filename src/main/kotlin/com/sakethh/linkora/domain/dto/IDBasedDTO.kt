package com.sakethh.linkora.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class IDBasedDTO(
    val id:Long,
    val correlation: Correlation,
    val eventTimestamp: Long
)
