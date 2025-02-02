package com.sakethh.linkora.domain.repository

import com.sakethh.linkora.domain.dto.AllTablesDTO
import com.sakethh.linkora.domain.dto.Tombstone

interface SyncRepo {
    suspend fun getTombstonesAfter(eventTimestamp: Long): List<Tombstone>
    suspend fun getUpdatesAfter(eventTimestamp: Long): AllTablesDTO
    suspend fun deleteEverything(): Result<Unit>
}