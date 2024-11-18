package com.sakethh.linkora

import com.sakethh.linkora.domain.tables.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun configureDatabase() {
    Database.connect("jdbc:sqlite:identifier.sqlite")
    transaction {
        SchemaUtils.create(
            ArchiveLinksTable,
            FoldersTable,
            HistoryLinksTable,
            ImportantLinksTable,
            SavedAndFolderLinksTable,
        )
    }
}