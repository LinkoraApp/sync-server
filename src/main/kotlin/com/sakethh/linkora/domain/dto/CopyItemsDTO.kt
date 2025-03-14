package com.sakethh.linkora.domain.dto

import com.sakethh.linkora.domain.LinkType
import kotlinx.serialization.Serializable

@Serializable
data class CopyItemsDTO(
    val folders: List<CopyFolderDTO>,
    val linkIds: Map<Long, Long>,// `key` belongs to the client, `value` belongs to this server's db
    val linkType: LinkType, val newParentFolderId: Long, val correlation: Correlation, val eventTimestamp: Long
)


@Serializable
data class CopyFolderDTO(
    val currentFolder: CurrentFolder,
    val links: List<FolderLink>,
    val childFolders: List<CopyFolderDTO>
)

@Serializable
data class CurrentFolder(
    val localId: Long, val remoteId: Long
)

typealias FolderLink = CurrentFolder