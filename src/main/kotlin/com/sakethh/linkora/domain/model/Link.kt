package com.sakethh.linkora.domain.model

import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.MediaType
import com.sakethh.linkora.domain.dto.tag.LinkTagDTO
import kotlinx.serialization.Serializable

@Serializable
data class Link(
    val id:Long,
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
    val eventTimestamp:Long,
    val linkTags: List<LinkTagDTO>,
)