package com.sakethh.linkora.data.repository

import com.sakethh.linkora.domain.dto.Tombstone
import com.sakethh.linkora.domain.repository.TombstoneRepo
import com.sakethh.linkora.domain.tables.TombstoneTable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class TombstoneRepoImpl : TombstoneRepo {
    override suspend fun getTombstonesAfter(timestamp: Long): List<Tombstone> {
        return transaction {
            TombstoneTable.selectAll().where {
                TombstoneTable.deletedAt.greater(timestamp)
            }.toList().map {
                Tombstone(
                    deletedAt = it[TombstoneTable.deletedAt],
                    operation = it[TombstoneTable.operation],
                    payload = Json.parseToJsonElement(it[TombstoneTable.payload])
                )
            }
        }
    }
}