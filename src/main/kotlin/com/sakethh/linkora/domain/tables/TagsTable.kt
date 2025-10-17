package com.sakethh.linkora.domain.tables

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

object TagsTable : LongIdTable(name = "tags") {
    val lastModified = long("last_modified")
    val name = text("name")
}