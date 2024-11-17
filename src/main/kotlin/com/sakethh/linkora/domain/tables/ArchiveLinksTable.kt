package com.sakethh.linkora.domain.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object ArchiveLinksTable : LongIdTable("ArchiveLinksTable") {
    val linkTitle = text("linkTitle")
    val webURL = text("webURL")
    val baseURL = text("baseURL")
    val imgURL = text("imgURL")
    val infoForSaving = text("note")
    val userAgent = text("userAgent").nullable()
}