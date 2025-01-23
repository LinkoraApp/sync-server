package com.sakethh.linkora.domain.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object PanelsTable : LongIdTable(name = "panels") {
    val panelName = text("panelName")
    val lastModified = text("last_modified")
}