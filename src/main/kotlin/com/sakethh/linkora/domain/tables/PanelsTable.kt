package com.sakethh.linkora.domain.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object PanelsTable : LongIdTable(name = "panels") {
    val panelName = text("panel_name")
    val lastModified = long("last_modified")
}