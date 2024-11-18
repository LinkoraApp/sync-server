package com.sakethh.linkora.domain.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object ArchiveLinksTable : LongIdTable(name = "ArchiveLinksTable") {
    val linkTitle = text(name = "linkTitle")
    val webURL = text(name = "webURL")
    val baseURL = text(name = "baseURL")
    val imgURL = text(name = "imgURL")
    val infoForSaving = text(name = "note")
    val userAgent = text(name = "userAgent").nullable()
}