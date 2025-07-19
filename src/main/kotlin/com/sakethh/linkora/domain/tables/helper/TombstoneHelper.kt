package com.sakethh.linkora.domain.tables.helper

import com.sakethh.linkora.domain.tables.TombstoneTable
import org.jetbrains.exposed.v1.jdbc.insert

object TombStoneHelper {
    fun insert(payload: String, operation: String, deletedAt: Long) {
        TombstoneTable.insert {
            it[this.deletedAt] = deletedAt
            it[this.payload] = payload
            it[this.operation] = operation
        }
    }
}