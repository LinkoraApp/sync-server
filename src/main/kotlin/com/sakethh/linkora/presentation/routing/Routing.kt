package com.sakethh.linkora.presentation.routing

import com.sakethh.linkora.Constants
import com.sakethh.linkora.authenticate
import com.sakethh.linkora.data.repository.*
import com.sakethh.linkora.domain.Route
import com.sakethh.linkora.domain.model.ServerConfig
import com.sakethh.linkora.domain.repository.*
import com.sakethh.linkora.presentation.routing.http.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database

fun Application.configureRouting(serverConfig: ServerConfig) {
   val serverInfoHtml =  object {}.javaClass.getResource("/raw/SERVER_IS_CONFIGURED.html")
    routing {
        authenticate {
            get("/") {
                call.respond(message = HttpStatusCode.OK, status = HttpStatusCode.OK)
            }
            get(Route.Sync.TEST_BEARER.name) {
                call.respond(message = HttpStatusCode.OK, status = HttpStatusCode.OK)
            }
        }
        get(Route.Sync.SERVER_IS_CONFIGURED.name) {
            val requiredHtml =
                serverInfoHtml!!.readText().replace(
                    oldValue = "###SYNC_SERVER_INFO###", newValue = """
                        <p>
                          The sync-server version is <strong>${Constants.SERVER_VERSION}</strong>.
                        </p>
                        <p>
                          You are currently connected to the <strong>${
                        Database.getDialectName(serverConfig.databaseUrl)
                    }</strong> database, which will be <strong>used by the server to store data</strong>.
                        </p>
                    """.trimIndent()
                )
            call.respondText(contentType = ContentType.Text.Html, text = requiredHtml)
        }
    }
    val linksRepo: LinksRepo = LinksRepoImpl()
    val panelsRepo: PanelsRepo = PanelsRepoImpl()
    val foldersRepo: FoldersRepo = FoldersRepoImpl(panelsRepo)
    val syncRepo: SyncRepo = SyncRepoImpl()
    val multiActionRepo: MultiActionRepo = MultiActionRepoImpl(foldersRepo)
    foldersRouting(foldersRepo)
    linksRouting(linksRepo)
    panelsRouting(panelsRepo)
    syncRouting(syncRepo)
    multiActionRouting(multiActionRepo)
}
