package com.sakethh.linkora.domain.dto.savedAndFolderLinks

import kotlinx.serialization.Serializable

@Serializable
data class UpdateLinkedFolderIDDto(
    val linkId: Long,
    val newParentFolderId: Long
)
