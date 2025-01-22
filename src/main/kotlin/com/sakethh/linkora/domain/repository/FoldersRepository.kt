package com.sakethh.linkora.domain.repository

import com.sakethh.linkora.domain.Folder
import com.sakethh.linkora.domain.dto.IDBasedDTO
import com.sakethh.linkora.domain.dto.folder.AddFolderDTO
import com.sakethh.linkora.domain.dto.folder.ChangeParentFolderDTO
import com.sakethh.linkora.domain.dto.folder.UpdateFolderNameDTO
import com.sakethh.linkora.domain.dto.folder.UpdateFolderNoteDTO
import com.sakethh.linkora.domain.dto.NewItemResponseDTO
import com.sakethh.linkora.utils.Result

typealias Message = String

interface FoldersRepository {
    suspend fun createFolder(addFolderDTO: AddFolderDTO): Result<NewItemResponseDTO>
    suspend fun deleteFolder(idBasedDTO: IDBasedDTO): Result<Message>
    suspend fun getChildFolders(idBasedDTO: IDBasedDTO): Result<List<Folder>>
    suspend fun getRootFolders(): Result<List<Folder>>
    suspend fun markAsArchive(idBasedDTO: IDBasedDTO): Result<Message>
    suspend fun markAsRegularFolder(idBasedDTO: IDBasedDTO): Result<Message>
    suspend fun changeParentFolder(changeParentFolderDTO: ChangeParentFolderDTO): Result<Message>
    suspend fun updateFolderName(updateFolderNameDTO: UpdateFolderNameDTO): Result<Message>

    suspend fun updateFolderNote(updateFolderNoteDTO: UpdateFolderNoteDTO): Result<Message>
    suspend fun deleteFolderNote(idBasedDTO: IDBasedDTO): Result<Message>
}