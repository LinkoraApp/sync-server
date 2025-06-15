package com.sakethh.linkora.presentation.routing

import com.sakethh.linkora.Constants
import com.sakethh.linkora.authenticate
import com.sakethh.linkora.data.repository.*
import com.sakethh.linkora.domain.Route
import com.sakethh.linkora.domain.model.ServerConfig
import com.sakethh.linkora.domain.repository.*
import com.sakethh.linkora.presentation.routing.http.*
import com.sakethh.linkora.utils.SysEnvKey
import com.sakethh.linkora.utils.useSysEnvValues
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import java.net.InetAddress

fun Application.configureRouting(serverConfig: ServerConfig, markdownManagerRepo: MarkdownManagerRepo) {
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
            val placeHolderValue =
                if ((useSysEnvValues().not() && serverConfig.hostAddress != InetAddress.getLocalHost().hostAddress) || (useSysEnvValues() && System.getenv(
                        SysEnvKey.LINKORA_HOST_ADDRESS.name
                    ) != InetAddress.getLocalHost().hostAddress)
                ) {
                    """### **Local Hosting & IPv4 Address**
- If you're **hosting locally**, ensure you're using an **IPv4 address** (${InetAddress.getLocalHost().hostAddress}) as `serverHost`.
- If using environment variables, set `${SysEnvKey.LINKORA_HOST_ADDRESS.name}` to `${InetAddress.getLocalHost().hostAddress}`.
- Otherwise, update `serverHost` in `linkoraConfig.json` so the Android app can connect.
- If you're **only using Linkora on Desktop**, no changes are needed."""
                } else {
                    ""
                }
            val requiredHtml = markdownManagerRepo.getRawHtmlBasedOnRawMD("The sync-server version is **${Constants.SERVER_VERSION}**.\n\nYou are currently connected to the **${Database.getDialectName(serverConfig.databaseUrl)}** database, which will be **used by the server to store data**.")+markdownManagerRepo.getRawHtmlBasedOnMDFile(
                fileLocation = "/raw/SERVER_IS_CONFIGURED.md", placeHolder = "#{PLACEHOLDER_1}" to placeHolderValue
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
