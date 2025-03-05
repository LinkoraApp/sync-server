package com.sakethh.linkora.domain.repository

import com.sakethh.linkora.domain.DeleteMultipleItemsDTO
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.ArchiveMultipleItemsDTO
import com.sakethh.linkora.domain.dto.TimeStampBasedResponse

interface MultiActionRepo {
    suspend fun archiveMultipleItems(archiveMultipleItemsDTO: ArchiveMultipleItemsDTO): Result<TimeStampBasedResponse>
    suspend fun deleteMultipleItems(deleteMultipleItemsDTO: DeleteMultipleItemsDTO): Result<TimeStampBasedResponse>
}