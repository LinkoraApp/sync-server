package com.sakethh.linkora.domain.dto.folder

import kotlinx.serialization.Serializable

@Serializable
data class ChangeParentFolderDTO(
    val folderId: Long,
    val newParentFolderId: Long,
    val correlationId:String
)
