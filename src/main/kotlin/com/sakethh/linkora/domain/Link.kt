package com.sakethh.linkora.domain

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
    val mediaType: MediaType
)