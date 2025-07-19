package com.sakethh.linkora.data

import com.sakethh.linkora.ServerConfiguration
import com.sakethh.linkora.domain.model.ServerConfig
import com.sakethh.linkora.domain.tables.*
import com.sakethh.linkora.utils.useSysEnvValues
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

private fun connectToADatabase(serverConfig: ServerConfig): Database {
    return Database.connect(
        url = serverConfig.databaseUrl, user = serverConfig.databaseUser, password = serverConfig.databasePassword
    )
}

fun configureDatabase() {
    val serverConfig = ServerConfiguration.readConfig()
    var database: Database? = null
    try {
        database = connectToADatabase(serverConfig)
        transaction {
            println("Connected to the database at ${this.db.url.substringAfter("jdbc:")}")
            SchemaUtils.createDatabase("linkora")
            createRequiredTables()
        }
        println("Linkora database is operational and accessible.")
    } catch (e: Exception) {

        if (e.message.toString().contains("Unknown database").not()) {
            database?.connector?.invoke()?.close()
        }

        if (e.message.toString().contains("requires autoCommit to be enabled")) {
            println("Enabling `autoCommit` as the database connected to requires it.")
            connectToADatabase(serverConfig).connector().autoCommit = true
            transaction {
                createRequiredTables()
            }
            return
        }
        if (e.message.toString().contains("Unknown database")) {
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
            connectToADatabase(serverConfig)
            transaction {
                createRequiredTables()
                println("Connected to the database at ${this.db.url}")
            }
        } else if (e.message?.contains("Database driver not found for") == true || e.message?.contains("Access denied") == true) {
            println(e.message)
            if (useSysEnvValues()) {
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
    SchemaUtils.create(*linkoraTables())
}

fun linkoraTables() = arrayOf(FoldersTable, LinksTable, TombstoneTable, PanelsTable, PanelFoldersTable)