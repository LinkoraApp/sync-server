package com.sakethh.linkora

import com.sakethh.linkora.data.repository.FoldersImplementation
import com.sakethh.linkora.data.repository.LinksImplementation
import com.sakethh.linkora.domain.repository.FoldersRepository
import com.sakethh.linkora.domain.repository.LinksRepository
import com.sakethh.linkora.routing.foldersRouting
import com.sakethh.linkora.routing.linksRouting
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respond(message = HttpStatusCode.OK.description, status = HttpStatusCode.OK)
        }
    }
    val foldersRepository: FoldersRepository = FoldersImplementation()
    val linksRepository: LinksRepository = LinksImplementation()

    foldersRouting(foldersRepository)
    linksRouting(linksRepository)
}
