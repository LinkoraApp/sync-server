package com.sakethh.linkora

import com.sakethh.linkora.domain.tables.*
import com.sakethh.linkora.utils.hostedOnRemote
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun configureDatabase() {
    val serverConfig = ServerConfiguration.readConfig()
    try {
        Database.connect(
            url = serverConfig.databaseUrl,
            user = serverConfig.databaseUser,
            password = serverConfig.databasePassword
        )
        transaction {
            println("Connected to the database at ${this.db.url}")
            SchemaUtils.createDatabase("linkora")
            createRequiredTables()
        }
        println("Linkora database is operational and accessible.")
    } catch (e: Exception) {
        if (e.message == "Unknown database 'linkora'") {
            println("Linkora database does not exist; proceeding with creation.")
            Database.connect(
                url = serverConfig.databaseUrl.substringBefore("/linkora"),
                user = serverConfig.databaseUser,
                password = serverConfig.databasePassword
            ).let {
                transaction {
                    SchemaUtils.createDatabase("linkora")
                }
                it.connector().close()
            }
            Database.connect(
                url = serverConfig.databaseUrl,
                user = serverConfig.databaseUser,
                password = serverConfig.databasePassword
            )
            transaction {
                createRequiredTables()
                println("Connected to the database at ${this.db.url}")
            }
        } else if (e.message?.contains("Database driver not found for") == true || e.message?.contains("Access denied") == true) {
            println(e.message)
            if (hostedOnRemote()) {
                println("\nManually causing the crash since the correct values aren't provided through environment variables.")
                throw e
            }
            println("\nConfigure the `linkoraConfig.json` file to properly set up the server and database connection.\nAfter completing the configuration, press 'Y' (uppercase) and hit Enter to proceed,\nor press '.' and hit Enter to provide the respective values. The rest will be handled by the Linkora server.")
            val inputChar = readln()
            if (inputChar == "Y") {
                configureDatabase()
            } else if (inputChar == ".") {
                ServerConfiguration.createConfig(forceWrite = true)
            } else {
                throw IllegalArgumentException()
            }
        } else {
            throw e
        }
    }
}

private fun createRequiredTables() {
    SchemaUtils.create(
        FoldersTable, LinksTable, TombstoneTable, PanelsTable, PanelFoldersTable
    )
}