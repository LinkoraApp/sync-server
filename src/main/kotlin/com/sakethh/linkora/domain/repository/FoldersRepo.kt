package com.sakethh.linkora.domain.repository

import com.sakethh.linkora.domain.dto.IDBasedDTO
import com.sakethh.linkora.domain.dto.NewItemResponseDTO
import com.sakethh.linkora.domain.dto.TimeStampBasedResponse
import com.sakethh.linkora.domain.dto.folder.*
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.MoveItemsDTO

interface FoldersRepo {
    suspend fun createFolder(addFolderDTO: AddFolderDTO): Result<NewItemResponseDTO>
    suspend fun updateFolder(folderDTO: FolderDTO): Result<TimeStampBasedResponse>
    suspend fun deleteFolder(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse>
    suspend fun getChildFolders(idBasedDTO: IDBasedDTO): Result<List<Folder>>
    suspend fun getRootFolders(): Result<List<Folder>>
    suspend fun markAsArchive(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse>
    suspend fun markAsRegularFolder(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse>
    suspend fun updateFolderName(updateFolderNameDTO: UpdateFolderNameDTO): Result<TimeStampBasedResponse>

    suspend fun updateFolderNote(updateFolderNoteDTO: UpdateFolderNoteDTO): Result<TimeStampBasedResponse>
    suspend fun deleteFolderNote(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse>
    suspend fun markSelectedFoldersAsRoot(markSelectedFoldersAsRootDTO: MarkSelectedFoldersAsRootDTO): Result<TimeStampBasedResponse>
}