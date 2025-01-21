package com.sakethh.linkora.domain.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object PanelFoldersTable : LongIdTable(name = "panel_folders") {
    val folderId = long(name = "folderId")
    val folderName = text(name = "folderName")
    val panelPosition = long(name = "panelPosition")
    val connectedPanelId = long(name = "connectedPanelId")
}