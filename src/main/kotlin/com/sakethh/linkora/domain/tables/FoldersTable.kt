package com.sakethh.linkora.domain.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object FoldersTable : LongIdTable(name = "folders_table") {
    val folderName = text(name = "folderName")
    val infoForSaving = text(name = "note")
    val parentFolderID = long(name = "parentFolderID").nullable()
    val isFolderArchived = bool(name = "isFolderArchived").default(false)
}