package com.sakethh.linkora.domain.dto.nonSavedLinksAndNonFolderLinks

import com.sakethh.linkora.domain.LinkType
import kotlinx.serialization.Serializable

@Serializable
data class NonSavedAndNonFolderLinkDTO(
    val linkType: LinkType,
    val id: Long,
    val linkTitle: String,
    val webURL: String,
    val baseURL: String,
    val imgURL: String,
    val infoForSaving: String,
    val userAgent: String?
)
