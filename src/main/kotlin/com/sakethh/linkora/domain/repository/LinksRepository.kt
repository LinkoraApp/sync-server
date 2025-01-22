package com.sakethh.linkora.domain.repository

import com.sakethh.linkora.domain.Link
import com.sakethh.linkora.domain.dto.IDBasedDTO
import com.sakethh.linkora.domain.dto.link.*
import com.sakethh.linkora.utils.Result

interface LinksRepository {
    suspend fun createANewLink(addLinkDTO: AddLinkDTO): Result<NewItemResponseDTO>
    suspend fun deleteALink(idBasedDTO: IDBasedDTO): Result<Message>
    suspend fun deleteLinksOfAFolder(idBasedDTO: IDBasedDTO): Result<Message>
    suspend fun updateLinkedFolderIdOfALink(updateLinkedFolderIDDto: UpdateLinkedFolderIDDto): Result<Message>
    suspend fun updateTitleOfTheLink(updateTitleOfTheLinkDTO: UpdateTitleOfTheLinkDTO): Result<Message>
    suspend fun updateNote(updateNoteOfALinkDTO: UpdateNoteOfALinkDTO): Result<Message>
    suspend fun updateUserAgent(updateLinkUserAgentDTO: UpdateLinkUserAgentDTO): Result<Message>
    suspend fun archiveALink(idBasedDTO: IDBasedDTO): Result<Message>
    suspend fun unArchiveALink(idBasedDTO: IDBasedDTO): Result<Message>
    suspend fun markALinkAsImp(idBasedDTO: IDBasedDTO):Result<Message>
    suspend fun markALinkAsNonImp(idBasedDTO: IDBasedDTO):Result<Message>
    suspend fun updateLink(linkDTO: LinkDTO):Result<Message>
}