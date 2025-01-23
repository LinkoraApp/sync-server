package com.sakethh.linkora.domain.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object FoldersTable : LongIdTable(name = "folders_table") {
    val lastModified = long("last_modified")
    val folderName = text(name = "folder_name")
    val note = text(name = "note")
    val parentFolderID = long(name = "parent_folder_id").nullable()
    val isFolderArchived = bool(name = "is_folder_archived").default(false)
}