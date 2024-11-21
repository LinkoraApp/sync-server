package com.sakethh.linkora.domain.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object FoldersTombstone : LongIdTable("folders_tombstone") {
    val lastModified = long("last_modified")
    val folderName = text(name = "folderName")
    val infoForSaving = text(name = "note")
    val parentFolderID = long(name = "parentFolderID").nullable()
    val isFolderArchived = bool(name = "isFolderArchived").default(false)
    val originId = long(name = "originId")
}