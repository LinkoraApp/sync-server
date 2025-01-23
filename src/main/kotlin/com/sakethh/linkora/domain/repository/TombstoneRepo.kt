package com.sakethh.linkora.domain.repository

import com.sakethh.linkora.domain.dto.Tombstone

interface TombstoneRepo {
    suspend fun getTombstonesAfter(timestamp: Long): List<Tombstone>
}