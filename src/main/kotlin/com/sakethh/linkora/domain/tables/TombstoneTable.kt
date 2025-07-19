package com.sakethh.linkora.domain.tables

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

object TombstoneTable : LongIdTable("tombstone") {
    val deletedAt = long("deleted_at")
    val operation = text("operation")
    val payload = text("payload")
}