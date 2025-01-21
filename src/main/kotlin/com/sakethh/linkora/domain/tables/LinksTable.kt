package com.sakethh.linkora.domain.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object LinksTable : LongIdTable(name = "links_table") {
    val lastModified = text("last_modified")
    val linkType = text("link_type")
    val linkTitle = text(name = "linkTitle")
    val url = text(name = "url")
    val baseURL = text(name = "baseURL")
    val imgURL = text(name = "imgURL")
    val note = text(name = "note")
    val idOfLinkedFolder = long(name = "idOfLinkedFolder").nullable()
    val userAgent = text(name = "userAgent").nullable()
    val mediaType = text(name = "mediaType")
    val markedAsImportant = bool("markedAsImportant")
}