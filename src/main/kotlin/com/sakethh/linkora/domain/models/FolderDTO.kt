package com.sakethh.linkora.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class FolderDTO(
    val id: Long? = null,
    val folderName: String,
    val infoForSaving: String,
    val parentFolderID: Long?,
    val isFolderArchived: Boolean
)
