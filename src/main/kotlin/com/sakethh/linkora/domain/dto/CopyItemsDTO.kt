package com.sakethh.linkora.domain.dto

import com.sakethh.linkora.domain.LinkType
import kotlinx.serialization.Serializable

@Serializable
data class CopyItemsDTO(
    val folderIds: Map<Long, Long>, // `key` belongs to the client, `value` belongs to this server's db
    val linkIds: Map<Long, Long>,// `key` belongs to the client, `value` belongs to this server's db
    val linkType: LinkType, val newParentFolderId: Long, val correlation: Correlation, val eventTimestamp: Long
)
