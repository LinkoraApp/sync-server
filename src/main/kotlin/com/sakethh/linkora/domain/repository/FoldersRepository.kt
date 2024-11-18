package com.sakethh.linkora.domain.repository

import com.sakethh.linkora.domain.dto.folder.FolderDTO
import com.sakethh.linkora.utils.RequestResultState

typealias Message = String

interface FoldersRepository {
    suspend fun createFolder(folderDTO: FolderDTO): RequestResultState<Message>
    suspend fun deleteFolder(folderId: Long): RequestResultState<Message>
    suspend fun getChildFolders(parentFolderId: Long): RequestResultState<List<FolderDTO>>
    suspend fun getRootFolders(): RequestResultState<List<FolderDTO>>
    suspend fun markAsArchive(folderId: Long): RequestResultState<Message>
    suspend fun markAsRegularFolder(folderId: Long): RequestResultState<Message>
    suspend fun changeParentFolder(folderId: Long, newParentFolderId: Long): RequestResultState<Message>
    suspend fun updateFolderName(folderId: Long, newFolderName: String): RequestResultState<Message>

    suspend fun updateFolderNote(folderId: Long, newNote: String): RequestResultState<Message>
    suspend fun deleteFolderNote(folderId: Long): RequestResultState<Message>
}