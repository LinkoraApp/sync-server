package com.sakethh.linkora.domain.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object SavedAndFolderLinksTable : LongIdTable(name = "SavedAndFolderLinksTable") {
    val linkTitle = text(name = "linkTitle")
    val webURL = text(name = "webURL")
    val baseURL = text(name = "baseURL")
    val imgURL = text(name = "imgURL")
    val infoForSaving = text(name = "note")
    val isLinkedWithSavedLinks = bool(name = "isLinkedWithSavedLinks")
    val isLinkedWithFolders = bool(name = "isLinkedWithFolders")
    val idOfLinkedFolder = long(name = "idOfLinkedFolder").nullable()
    val userAgent = text(name = "userAgent").nullable()
}