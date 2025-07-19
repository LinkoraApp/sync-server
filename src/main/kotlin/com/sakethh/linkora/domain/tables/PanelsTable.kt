package com.sakethh.linkora.domain.tables

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

object PanelsTable : LongIdTable(name = "panels") {
    val panelName = text("panel_name")
    val lastModified = long("last_modified")
}