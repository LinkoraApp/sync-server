package com.sakethh.linkora

import com.sakethh.linkora.data.repository.FoldersImplementation
import com.sakethh.linkora.data.repository.NonSavedAndNonFolderLinksImplementation
import com.sakethh.linkora.data.repository.SavedAndFolderLinksImplementation
import com.sakethh.linkora.domain.repository.FoldersRepository
import com.sakethh.linkora.domain.repository.NonSavedAndNonFolderLinksRepository
import com.sakethh.linkora.domain.repository.SavedAndFolderLinksRepository
import com.sakethh.linkora.routing.foldersRouting
import com.sakethh.linkora.routing.nonSavedAndNonFolderLinksRouting
import com.sakethh.linkora.routing.savedAndFolderLinksRouting
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Application.configureRouting() {
    routing {
        get("/") {
            val json = Json { prettyPrint = true }
            call.respond(message = json.encodeToString(ServerConfiguration.readConfig()), status = HttpStatusCode.OK)
        }
    }
    val foldersRepository: FoldersRepository = FoldersImplementation()
    val savedAndFolderLinksRepository: SavedAndFolderLinksRepository = SavedAndFolderLinksImplementation()
    val nonSavedAndNonFolderLinksRepository: NonSavedAndNonFolderLinksRepository =
        NonSavedAndNonFolderLinksImplementation()

    foldersRouting(foldersRepository)
    savedAndFolderLinksRouting(savedAndFolderLinksRepository)
    nonSavedAndNonFolderLinksRouting(nonSavedAndNonFolderLinksRepository)
}
