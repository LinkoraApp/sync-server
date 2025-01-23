package com.sakethh.linkora.domain.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object Tombstone : LongIdTable("tombstone") {
    val deletedAt = text("deletedAt")
    val operation = text("operation")
    val payload = text("payload")
}