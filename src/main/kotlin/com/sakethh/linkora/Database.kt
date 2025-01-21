package com.sakethh.linkora

import com.sakethh.linkora.domain.tables.*
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
            println("\nConfigure the `linkoraConfig.json` file to properly set up the server and database connection.\nAfter completing the configuration, press 'Y' (uppercase) and hit Enter to proceed,\nor press '.' and hit Enter to provide the respective values. The rest will be handled by the Linkora server.")
            val inputChar = readln()
            if (inputChar == "Y") {
                configureDatabase()
            } else if (inputChar == ".") {
                ServerConfiguration.createConfig(forceWrite = true)
            } else {
                val invalidCharText = listOf(
                    "\n\nWas waiting for a 'Y', but since you're too cool for that, crashing the setup like it's no big deal. Enjoy the chaos! 💥\n",
                    "\n\nExpected a nice 'Y' to play along, but since we’re not on the same page, crashing the setup without hesitation! 😏\n",
                    "\n\nWaiting for a 'Y' to keep things moving, but since it’s missing... BOOM, setup crash incoming! 💣\n",
                    "\n\nWas hoping for a friendly 'Y', but nope—no mercy! Forcing a crash now. You asked for it! 😜",
                    "\n\nYou didn’t hit me with that 'Y'? Guess what? Setup crashing like it’s a Nas verse—smooth but hard-hitting. Ain’t no mercy, fam. 🔨\n",
                    "\n\nNo 'Y'? You know what that means? Time to crash this setup like Nas’ flow—effortless but packs a punch. Get ready for it. ⚡\n"
                )
                println(invalidCharText.random())
                throw IllegalArgumentException()
            }
        } else {
            throw e
        }
    }
}

private fun createRequiredTables() {
    SchemaUtils.create(
        FoldersTable, LinksTable, FoldersTombstone, LinksTombstone, PanelsTable, PanelFoldersTable
    )
}