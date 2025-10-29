package com.sakethh.linkora.domain.dto.link

import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.MediaType
import com.sakethh.linkora.domain.dto.Correlation
import com.sakethh.linkora.domain.dto.tag.LinkTagDTO
import kotlinx.serialization.Serializable

@Serializable
data class LinkDTO(
    val id: Long,
    val linkType: LinkType,
    val title: String,
    val url: String,
    val baseURL: String,
    val imgURL: String,
    val note: String,
    val idOfLinkedFolder: Long?,
    val userAgent: String?,
    val markedAsImportant: Boolean,
    val mediaType: MediaType,
    val correlation: Correlation,
    val linkTags: List<LinkTagDTO>,
    val eventTimestamp: Long
)