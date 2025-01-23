package com.sakethh.linkora.domain.tables.helper

import com.sakethh.linkora.domain.tables.Tombstone
import org.jetbrains.exposed.sql.insert
import java.time.Instant

object TombStoneHelper {
    fun insert(payload: String, operation: String) {
        Tombstone.insert {
            it[this.deletedAt] = Instant.now().epochSecond
            it[this.payload] = payload
            it[this.operation] = operation
        }
    }
}