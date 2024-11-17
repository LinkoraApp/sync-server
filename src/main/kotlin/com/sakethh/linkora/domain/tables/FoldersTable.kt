package com.sakethh.linkora.domain.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object FoldersTable : LongIdTable("Folders") {
    var folderName = text("folderName")
    var infoForSaving = text("note")
    var parentFolderID = long("parentFolderID").nullable()
    var isFolderArchived = bool("isFolderArchived").default(false)
}