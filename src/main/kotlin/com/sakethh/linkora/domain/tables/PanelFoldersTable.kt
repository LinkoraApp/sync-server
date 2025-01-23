package com.sakethh.linkora.domain.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object PanelFoldersTable : LongIdTable(name = "panel_folders") {
    val folderId = long(name = "folder_id")
    val folderName = text(name = "folder_name")
    val panelPosition = long(name = "panel_position")
    val connectedPanelId = long(name = "connected_panel_id")
    val lastModified = long("last_modified")
}