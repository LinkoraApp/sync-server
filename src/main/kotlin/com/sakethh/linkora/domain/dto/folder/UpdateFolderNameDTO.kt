package com.sakethh.linkora.domain.dto.folder

import kotlinx.serialization.Serializable

@Serializable
data class UpdateFolderNameDTO(
    val folderId: Long,
    val newFolderName: String,
    val correlationId:String
)
