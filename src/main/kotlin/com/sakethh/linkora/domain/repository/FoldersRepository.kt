package com.sakethh.linkora.domain.repository

import com.sakethh.linkora.domain.dto.folder.FolderDTO
import com.sakethh.linkora.domain.dto.link.NewItemResponseDTO
import com.sakethh.linkora.utils.Result

typealias Message = String

interface FoldersRepository {
    suspend fun createFolder(folderDTO: FolderDTO): Result<NewItemResponseDTO>
    suspend fun deleteFolder(folderId: Long): Result<Message>
    suspend fun getChildFolders(parentFolderId: Long): Result<List<FolderDTO>>
    suspend fun getRootFolders(): Result<List<FolderDTO>>
    suspend fun markAsArchive(folderId: Long): Result<Message>
    suspend fun markAsRegularFolder(folderId: Long): Result<Message>
    suspend fun changeParentFolder(folderId: Long, newParentFolderId: Long): Result<Message>
    suspend fun updateFolderName(folderId: Long, newFolderName: String): Result<Message>

    suspend fun updateFolderNote(folderId: Long, newNote: String): Result<Message>
    suspend fun deleteFolderNote(folderId: Long): Result<Message>
}