package com.sakethh.linkora.domain.repository

import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.dto.IDBasedDTO
import com.sakethh.linkora.domain.dto.NewItemResponseDTO
import com.sakethh.linkora.domain.dto.TimeStampBasedResponse
import com.sakethh.linkora.domain.dto.folder.AddFolderDTO
import com.sakethh.linkora.domain.dto.folder.ChangeParentFolderDTO
import com.sakethh.linkora.domain.dto.folder.UpdateFolderNameDTO
import com.sakethh.linkora.domain.dto.folder.UpdateFolderNoteDTO
import com.sakethh.linkora.utils.Result

interface FoldersRepository {
    suspend fun createFolder(addFolderDTO: AddFolderDTO): Result<NewItemResponseDTO>
    suspend fun deleteFolder(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse>
    suspend fun getChildFolders(idBasedDTO: IDBasedDTO): Result<List<Folder>>
    suspend fun getRootFolders(): Result<List<Folder>>
    suspend fun markAsArchive(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse>
    suspend fun markAsRegularFolder(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse>
    suspend fun changeParentFolder(changeParentFolderDTO: ChangeParentFolderDTO): Result<TimeStampBasedResponse>
    suspend fun updateFolderName(updateFolderNameDTO: UpdateFolderNameDTO): Result<TimeStampBasedResponse>

    suspend fun updateFolderNote(updateFolderNoteDTO: UpdateFolderNoteDTO): Result<TimeStampBasedResponse>
    suspend fun deleteFolderNote(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse>
}