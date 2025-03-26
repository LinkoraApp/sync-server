package com.sakethh.linkora.domain.repository

import com.sakethh.linkora.domain.DeleteMultipleItemsDTO
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.*
import com.sakethh.linkora.domain.dto.folder.MarkItemsRegularDTO

interface MultiActionRepo {
    suspend fun archiveMultipleItems(archiveMultipleItemsDTO: ArchiveMultipleItemsDTO): Result<TimeStampBasedResponse>
    suspend fun markItemsAsRegular(markItemsRegularDTO: MarkItemsRegularDTO): Result<TimeStampBasedResponse>
    suspend fun deleteMultipleItems(deleteMultipleItemsDTO: DeleteMultipleItemsDTO): Result<TimeStampBasedResponse>
    suspend fun moveMultipleItems(moveItemsDTO: MoveItemsDTO): Result<TimeStampBasedResponse>
    suspend fun copyMultipleItems(copyItemsDTO: CopyItemsDTO): Result<CopyItemsResponseDTO>
}