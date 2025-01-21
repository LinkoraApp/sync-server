package com.sakethh.linkora.domain.repository

import com.sakethh.linkora.domain.Folder
import com.sakethh.linkora.domain.dto.folder.AddFolderDTO
import com.sakethh.linkora.domain.dto.link.NewItemResponseDTO
import com.sakethh.linkora.utils.Result

typealias Message = String

interface FoldersRepository {
    suspend fun createFolder(addFolderDTO: AddFolderDTO): Result<NewItemResponseDTO>
    suspend fun deleteFolder(folderId: Long): Result<Message>
    suspend fun getChildFolders(parentFolderId: Long): Result<List<Folder>>
    suspend fun getRootFolders(): Result<List<Folder>>
    suspend fun markAsArchive(folderId: Long): Result<Message>
    suspend fun markAsRegularFolder(folderId: Long): Result<Message>
    suspend fun changeParentFolder(folderId: Long, newParentFolderId: Long): Result<Message>
    suspend fun updateFolderName(folderId: Long, newFolderName: String): Result<Message>

    suspend fun updateFolderNote(folderId: Long, newNote: String): Result<Message>
    suspend fun deleteFolderNote(folderId: Long): Result<Message>
}