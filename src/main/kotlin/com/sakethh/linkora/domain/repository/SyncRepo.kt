package com.sakethh.linkora.domain.repository

import com.sakethh.linkora.domain.dto.AllTablesDTO
import com.sakethh.linkora.domain.dto.Tombstone

interface SyncRepo {
    suspend fun getTombstonesAfter(timestamp: Long): List<Tombstone>
    suspend fun getUpdatesAfter(timestamp: Long): AllTablesDTO
}