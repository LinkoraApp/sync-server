package com.sakethh.linkora.domain.tables

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

object LinksTable : LongIdTable(name = "links_table") {
    val lastModified = long("last_modified")
    val linkType = text("link_type")
    val linkTitle = text(name = "link_title")
    val url = text(name = "url")
    val baseURL = text(name = "base_url")
    val imgURL = text(name = "img_url")
    val note = text(name = "note")
    val idOfLinkedFolder = long(name = "id_of_linked_folder").nullable()
    val userAgent = text(name = "user_agent").nullable()
    val mediaType = text(name = "media_type")
    val markedAsImportant = bool("marked_as_important")
}