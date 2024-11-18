package com.sakethh.linkora.domain.repository

import com.sakethh.linkora.domain.dto.FolderDTO

interface FoldersRepository {
    suspend fun createFolder(folderDTO: FolderDTO): Long
    suspend fun deleteFolder(folderId: Long)
    suspend fun getChildFolders(parentFolderId: Long): List<FolderDTO>
    suspend fun getRootFolders(): List<FolderDTO>
    suspend fun markAsArchive(folderId: Long)
    suspend fun markAsRegularFolder(folderId: Long)
    suspend fun changeParentFolder(folderId: Long, newParentFolderId: Long)
    suspend fun updateFolderName(folderId: Long, newFolderName: String)

    suspend fun updateFolderNote(folderId: Long, note: String)
    suspend fun deleteFolderNote(folderId: Long)
}