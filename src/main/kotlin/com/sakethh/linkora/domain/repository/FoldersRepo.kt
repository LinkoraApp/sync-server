package com.sakethh.linkora.domain.repository

import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.IDBasedDTO
import com.sakethh.linkora.domain.dto.NewItemResponseDTO
import com.sakethh.linkora.domain.dto.TimeStampBasedResponse
import com.sakethh.linkora.domain.dto.folder.AddFolderDTO
import com.sakethh.linkora.domain.dto.folder.MarkSelectedFoldersAsRootDTO
import com.sakethh.linkora.domain.dto.folder.UpdateFolderNameDTO
import com.sakethh.linkora.domain.dto.folder.UpdateFolderNoteDTO
import com.sakethh.linkora.domain.model.Folder

interface FoldersRepo {
    suspend fun createFolder(addFolderDTO: AddFolderDTO): Result<NewItemResponseDTO>
    suspend fun deleteFolder(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse>
    suspend fun getChildFolders(idBasedDTO: IDBasedDTO): Result<List<Folder>>
    suspend fun getRootFolders(): Result<List<Folder>>
    suspend fun getAllFolders(): Result<List<Folder>>
    suspend fun markAsArchive(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse>
    suspend fun markAsRegularFolder(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse>
    suspend fun updateFolderName(updateFolderNameDTO: UpdateFolderNameDTO): Result<TimeStampBasedResponse>

    suspend fun updateFolderNote(updateFolderNoteDTO: UpdateFolderNoteDTO): Result<TimeStampBasedResponse>
    suspend fun deleteFolderNote(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse>
    suspend fun markSelectedFoldersAsRoot(markSelectedFoldersAsRootDTO: MarkSelectedFoldersAsRootDTO): Result<TimeStampBasedResponse>
}