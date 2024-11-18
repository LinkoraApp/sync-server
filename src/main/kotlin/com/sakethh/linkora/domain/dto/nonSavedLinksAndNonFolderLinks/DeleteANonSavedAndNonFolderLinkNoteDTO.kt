package com.sakethh.linkora.domain.dto.nonSavedLinksAndNonFolderLinks

import com.sakethh.linkora.domain.LinkType
import kotlinx.serialization.Serializable

@Serializable
data class DeleteANonSavedAndNonFolderLinkNoteDTO(
    val linkType: LinkType,
    val linkId: Long
)
