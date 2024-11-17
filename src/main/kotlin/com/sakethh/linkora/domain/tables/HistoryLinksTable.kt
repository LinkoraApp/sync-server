package com.sakethh.linkora.domain.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object HistoryLinksTable : LongIdTable("history_links") {
    val linkTitle = text("linkTitle")
    val webURL = text("webURL")
    val baseURL = text("baseURL")
    val imgURL = text("imgURL")
    val infoForSaving = text("note")
    val userAgent = text("userAgent").nullable()
}