package com.sakethh.linkora.domain.dto.savedAndFolderLinks

import kotlinx.serialization.Serializable

@Serializable
data class SavedAndFolderLinksDTO(
    val id: Long,
    val linkTitle: String,
    val webURL: String,
    val baseURL: String,
    val imgURL: String,
    val infoForSaving: String,
    val isLinkedWithSavedLinks: Boolean,
    val isLinkedWithFolders: Boolean,
    val idOfLinkedFolder: Long?,
    val userAgent: String?,
)
