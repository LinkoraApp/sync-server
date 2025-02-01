package com.sakethh.linkora

import com.sakethh.linkora.data.repository.FoldersImplementation
import com.sakethh.linkora.data.repository.LinksImplementation
import com.sakethh.linkora.data.repository.PanelsRepoImpl
import com.sakethh.linkora.data.repository.SyncRepoImpl
import com.sakethh.linkora.domain.repository.FoldersRepository
import com.sakethh.linkora.domain.repository.LinksRepository
import com.sakethh.linkora.domain.repository.PanelsRepository
import com.sakethh.linkora.domain.repository.SyncRepo
import com.sakethh.linkora.domain.routes.SyncRoute
import com.sakethh.linkora.routing.foldersRouting
import com.sakethh.linkora.routing.linksRouting
import com.sakethh.linkora.routing.panelsRouting
import com.sakethh.linkora.routing.syncRouting
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respond(message = HttpStatusCode.OK, status = HttpStatusCode.OK)
        }

        authenticate(Security.BEARER.name) {
            get(SyncRoute.TEST_BEARER.name) {
                call.respond(message = HttpStatusCode.OK, status = HttpStatusCode.OK)
            }
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
