package com.sakethh.linkora.domain.repository

import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.dto.link.*
import com.sakethh.linkora.utils.RequestResultState

interface LinksRepository {
    suspend fun createANewLink(linkDTO: LinkDTO): RequestResultState<Message>
    suspend fun deleteALink(deleteALinkDTO: DeleteALinkDTO): RequestResultState<Message>
    suspend fun deleteLinksOfAFolder(folderId: Long): RequestResultState<Message>
    suspend fun updateLinkedFolderIdOfALink(updateLinkedFolderIDDto: UpdateLinkedFolderIDDto): RequestResultState<Message>
    suspend fun updateTitleOfTheLink(updateTitleOfTheLinkDTO: UpdateTitleOfTheLinkDTO): RequestResultState<Message>
    suspend fun updateNote(updateNoteOfALinkDTO: UpdateNoteOfALinkDTO): RequestResultState<Message>
    suspend fun updateUserAgent(updateLinkUserAgentDTO: UpdateLinkUserAgentDTO): RequestResultState<Message>
    suspend fun getLinks(linkType: LinkType): RequestResultState<List<LinkDTO>>
    suspend fun getLinksFromAFolder(folderId: Long): RequestResultState<List<LinkDTO>>
}