package com.sakethh.linkora

import com.sakethh.linkora.domain.tables.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun configureDatabase(url: String, user: String, password: String) {
    try {
        Database.connect(url = url, user = user, password = password)
        transaction {
            println("Connected to the database at ${this.db.url}")
            SchemaUtils.createDatabase("linkora")
            SchemaUtils.create(
                ArchiveLinksTable,
                FoldersTable,
                HistoryLinksTable,
                ImportantLinksTable,
                SavedAndFolderLinksTable,
            )
        }
        println("Linkora database is operational and accessible.")
    } catch (e: Exception) {
        if (e.message == "Unknown database 'linkora'") {
            println("Linkora database does not exist; proceeding with creation.")
            Database.connect(url = url.substringBefore("/linkora"), user = user, password = password).let {
                transaction {
                    SchemaUtils.createDatabase("linkora")
                }
                it.connector().close()
            }
            Database.connect(url = url, user = user, password = password)
            transaction {
                println("Connected to the database at ${this.db.url}")
                SchemaUtils.create(
                    ArchiveLinksTable,
                    FoldersTable,
                    HistoryLinksTable,
                    ImportantLinksTable,
                    SavedAndFolderLinksTable,
                )
            }
        } else {
            throw e
        }
    }
}