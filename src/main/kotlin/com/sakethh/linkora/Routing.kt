package com.sakethh.linkora

import com.sakethh.linkora.data.repository.FoldersImplementation
import com.sakethh.linkora.data.repository.LinksImplementation
import com.sakethh.linkora.data.repository.PanelsRepoImpl
import com.sakethh.linkora.domain.repository.FoldersRepository
import com.sakethh.linkora.domain.repository.LinksRepository
import com.sakethh.linkora.domain.repository.PanelsRepository
import com.sakethh.linkora.routing.foldersRouting
import com.sakethh.linkora.routing.linksRouting
import com.sakethh.linkora.routing.panelsRouting
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respond(message = HttpStatusCode.OK, status = HttpStatusCode.OK)
        }

        authenticate(Security.BEARER.name) {
            get("/testBearer") {
                call.respond(message = HttpStatusCode.OK, status = HttpStatusCode.OK)
            }
        }
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
    }
    val linksRepository: LinksRepository = LinksImplementation()
    val foldersRepository: FoldersRepository = FoldersImplementation(linksRepository = linksRepository)
    val panelsRepository: PanelsRepository = PanelsRepoImpl()
    foldersRouting(foldersRepository)
    linksRouting(linksRepository)
    panelsRouting(panelsRepository)
}
