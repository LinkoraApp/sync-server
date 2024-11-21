package com.sakethh.linkora.domain.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object LinksTombstone : LongIdTable("links_tombstone") {
    val lastModified = text("last_modified")
    val originId = long("originId")
    val linkType = text("link_type")
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