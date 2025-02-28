package com.sakethh.linkora.domain.repository

import com.sakethh.linkora.domain.dto.IDBasedDTO
import com.sakethh.linkora.domain.dto.NewItemResponseDTO
import com.sakethh.linkora.domain.dto.TimeStampBasedResponse
import com.sakethh.linkora.domain.dto.link.*
import com.sakethh.linkora.utils.Result

interface LinksRepository {
    suspend fun createANewLink(addLinkDTO: AddLinkDTO): Result<NewItemResponseDTO>
    suspend fun deleteALink(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse>
    suspend fun updateLinkedFolderIdOfALink(updateLinkedFolderIDDto: UpdateLinkedFolderIDDto): Result<TimeStampBasedResponse>
    suspend fun updateTitleOfTheLink(updateTitleOfTheLinkDTO: UpdateTitleOfTheLinkDTO): Result<TimeStampBasedResponse>
    suspend fun updateNote(updateNoteOfALinkDTO: UpdateNoteOfALinkDTO): Result<TimeStampBasedResponse>
    suspend fun updateUserAgent(updateLinkUserAgentDTO: UpdateLinkUserAgentDTO): Result<TimeStampBasedResponse>
    suspend fun archiveALink(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse>
    suspend fun unArchiveALink(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse>
    suspend fun markALinkAsImp(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse>
    suspend fun markALinkAsNonImp(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse>
    suspend fun updateLink(linkDTO: LinkDTO): Result<TimeStampBasedResponse>
    suspend fun deleteDuplicateLinks(deleteDuplicateLinksDTO: DeleteDuplicateLinksDTO): Result<TimeStampBasedResponse>
    suspend fun moveLinks(moveLinksDTO: MoveLinksDTO): Result<TimeStampBasedResponse>
}