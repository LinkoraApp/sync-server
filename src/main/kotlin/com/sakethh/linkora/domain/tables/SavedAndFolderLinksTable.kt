package com.sakethh.linkora.domain.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object SavedAndFolderLinksTable : LongIdTable("saved_links_and_folder_links") {
    val linkTitle = text("linkTitle")
    val webURL = text("webURL")
    val baseURL = text("baseURL")
    val imgURL = text("imgURL")
    val infoForSaving = text("note")
    val isLinkedWithSavedLinks = bool("isLinkedWithSavedLinks")
    val isLinkedWithFolders = bool("isLinkedWithFolders")
    val idOfLinkedFolder = long("idOfLinkedFolder").nullable()
    val userAgent = text("userAgent").nullable()
}