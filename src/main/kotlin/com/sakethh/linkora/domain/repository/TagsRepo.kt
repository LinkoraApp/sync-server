package com.sakethh.linkora.domain.repository

import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.IDBasedDTO
import com.sakethh.linkora.domain.dto.NewItemResponseDTO
import com.sakethh.linkora.domain.dto.TimeStampBasedResponse
import com.sakethh.linkora.domain.dto.tag.CreateTagDTO
import com.sakethh.linkora.domain.dto.tag.RenameTagDTO
import com.sakethh.linkora.domain.model.Tag

interface TagsRepo {
    suspend fun createATag(createTagDTO: CreateTagDTO): Result<NewItemResponseDTO>
    suspend fun renameATag(renameTagDTO: RenameTagDTO): Result<TimeStampBasedResponse>
    suspend fun deleteATag(idBasedDTO: IDBasedDTO): Result<TimeStampBasedResponse>
    suspend fun getTags(): Result<List<Tag>>
}