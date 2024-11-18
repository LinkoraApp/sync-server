package com.sakethh.linkora.domain.repository

import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.dto.nonSavedLinksAndNonFolderLinks.NonSavedAndNonFolderLinkDTO
import com.sakethh.linkora.utils.RequestResultState

interface NonSavedAndNonFolderLinksRepository {
    suspend fun createANewLink(
        linkType: LinkType,
        nonSavedAndNonFolderLinkDTO: NonSavedAndNonFolderLinkDTO
    ): RequestResultState<Message>

    suspend fun deleteANewLink(linkType: LinkType, linkId: Long): RequestResultState<Message>
    suspend fun getAllLinks(linkType: LinkType): RequestResultState<List<NonSavedAndNonFolderLinkDTO>>
    suspend fun updateLinkTitle(linkType: LinkType, linkId: Long, newTitle: String): RequestResultState<Message>
    suspend fun updateLinkNote(linkType: LinkType, linkId: Long, newNote: String): RequestResultState<Message>
    suspend fun deleteLinkNote(linkType: LinkType, linkId: Long): RequestResultState<Message>
}