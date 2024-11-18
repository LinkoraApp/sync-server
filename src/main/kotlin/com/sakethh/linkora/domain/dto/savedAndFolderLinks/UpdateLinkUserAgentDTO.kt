package com.sakethh.linkora.domain.dto.savedAndFolderLinks

import kotlinx.serialization.Serializable

@Serializable
data class UpdateLinkUserAgentDTO(
    val linkId: Long,
    val userAgent: String
)
