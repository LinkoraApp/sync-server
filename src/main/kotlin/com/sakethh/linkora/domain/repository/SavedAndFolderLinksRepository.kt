package com.sakethh.linkora.domain.repository

import com.sakethh.linkora.domain.dto.SavedAndFolderLinksDTO
import com.sakethh.linkora.utils.RequestResultState

interface SavedAndFolderLinksRepository {
    suspend fun createANewLink(savedAndFolderLinksDTO: SavedAndFolderLinksDTO): RequestResultState<Message>
    suspend fun deleteALink(linkId: Long): RequestResultState<Message>
    suspend fun deleteLinksOfAFolder(folderId: Long): RequestResultState<Message>
    suspend fun updateLinkedFolderId(linkId: Long, newParentFolderId: Long): RequestResultState<Message>
    suspend fun updateTitleOfTheLink(linkId: Long, newTitleOfTheLink: String): RequestResultState<Message>
    suspend fun updateNote(linkId: Long, newNote: String): RequestResultState<Message>
    suspend fun deleteNote(linkId: Long): RequestResultState<Message>
    suspend fun updateUserAgent(linkId: Long, userAgent: String): RequestResultState<Message>
    suspend fun getAllLinks(): RequestResultState<List<SavedAndFolderLinksDTO>>
    suspend fun getSavedLinks(): RequestResultState<List<SavedAndFolderLinksDTO>>
    suspend fun getLinksFromAFolder(folderId: Long): RequestResultState<List<SavedAndFolderLinksDTO>>
}