package com.sakethh.linkora.presentation.routing

import com.sakethh.linkora.Security
import com.sakethh.linkora.data.repository.FoldersImplementation
import com.sakethh.linkora.data.repository.LinksImplementation
import com.sakethh.linkora.data.repository.PanelsRepoImpl
import com.sakethh.linkora.data.repository.SyncRepoImpl
import com.sakethh.linkora.domain.model.ServerConfig
import com.sakethh.linkora.domain.repository.*
import com.sakethh.linkora.domain.routes.SyncRoute
import com.sakethh.linkora.presentation.routing.http.foldersRouting
import com.sakethh.linkora.presentation.routing.http.linksRouting
import com.sakethh.linkora.presentation.routing.http.panelsRouting
import com.sakethh.linkora.presentation.routing.http.syncRouting
import com.sakethh.linkora.utils.SysEnvKey
import com.sakethh.linkora.utils.useSysEnvValues
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.net.InetAddress

fun Application.configureRouting(serverConfig: ServerConfig, markdownManagerRepo: MarkdownManagerRepo) {
    routing {
        authenticate(Security.BEARER.name) {
            get("/") {
                call.respond(message = HttpStatusCode.OK, status = HttpStatusCode.OK)
            }
            get(SyncRoute.TEST_BEARER.name) {
                call.respond(message = HttpStatusCode.OK, status = HttpStatusCode.OK)
            }
        }
        get(SyncRoute.SERVER_IS_CONFIGURED.name) {
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
            val requiredHtml = markdownManagerRepo.getRawHtmlBasedOnMD(
                fileLocation = "/raw/SERVER_IS_CONFIGURED.md", placeHolder = "#{PLACEHOLDER_1}" to placeHolderValue
            )
            call.respondText(contentType = ContentType.Text.Html, text = requiredHtml)
        }
    }
    val linksRepository: LinksRepository = LinksImplementation()
    val foldersRepository: FoldersRepository = FoldersImplementation()
    val panelsRepository: PanelsRepository = PanelsRepoImpl()
    val tombstoneRouting: SyncRepo = SyncRepoImpl()
    foldersRouting(foldersRepository)
    linksRouting(linksRepository)
    panelsRouting(panelsRepository)
    syncRouting(tombstoneRouting)
}
