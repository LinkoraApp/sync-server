package com.sakethh.linkora

import com.sakethh.linkora.data.repository.FoldersImplementation
import com.sakethh.linkora.data.repository.SavedAndFolderLinksImplementation
import com.sakethh.linkora.domain.repository.FoldersRepository
import com.sakethh.linkora.domain.repository.SavedAndFolderLinksRepository
import com.sakethh.linkora.routing.foldersRouting
import com.sakethh.linkora.routing.savedAndFolderLinksRouting
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respond(status = HttpStatusCode.OK, message = HttpStatusCode.OK.description)
        }
    }
    val foldersRepository: FoldersRepository = FoldersImplementation()
    val savedAndFolderLinksRepository: SavedAndFolderLinksRepository = SavedAndFolderLinksImplementation()

    foldersRouting(foldersRepository)
    savedAndFolderLinksRouting(savedAndFolderLinksRepository)
}
