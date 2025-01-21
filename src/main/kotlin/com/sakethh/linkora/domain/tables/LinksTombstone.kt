package com.sakethh.linkora.domain.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object LinksTombstone : LongIdTable("links_tombstone") {
    val lastModified = text("last_modified")
    val originId = long("originId") // the id which has been used to save the link in the remote database
    val linkType = text("link_type")
    val linkTitle = text(name = "linkTitle")
    val webURL = text(name = "webURL")
    val baseURL = text(name = "baseURL")
    val imgURL = text(name = "imgURL")
    val infoForSaving = text(name = "note")
    val idOfLinkedFolder = long(name = "idOfLinkedFolder").nullable()
    val userAgent = text(name = "userAgent").nullable()
    val markedAsImportant = bool("markedAsImportant")
}