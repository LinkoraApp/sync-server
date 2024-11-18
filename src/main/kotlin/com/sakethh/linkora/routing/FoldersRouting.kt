package com.sakethh.linkora.routing

import com.sakethh.linkora.domain.dto.FolderDTO
import com.sakethh.linkora.domain.repository.FoldersRepository
import com.sakethh.linkora.domain.routes.FolderRoute
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Application.foldersRouting(foldersRepository: FoldersRepository) {
    routing {
        post<FolderDTO>(FolderRoute.CREATE_FOLDER.name) { folderDTO ->
            foldersRepository.createFolder(folderDTO).let {
                call.respond(status = HttpStatusCode.OK, message = "Folder created successfully with id = $it")
            }
        }

        post<Long>(FolderRoute.DELETE_FOLDER.name) { folderId ->
            foldersRepository.deleteFolder(folderId)
        }

        post<Long>(FolderRoute.GET_CHILD_FOLDERS.name) { folderId ->
            call.respond(foldersRepository.getChildFolders(folderId))
        }

        get(FolderRoute.GET_ROOT_FOLDERS.name) {
            call.respond(foldersRepository.getRootFolders())
        }

        post<Long>(FolderRoute.MARK_AS_ARCHIVE.name) { folderId ->
            call.respond(foldersRepository.markAsArchive(folderId))
        }

        post<Long>(FolderRoute.MARK_AS_REGULAR_FOLDER.name) { folderId ->
            call.respond(foldersRepository.markAsRegularFolder(folderId))
        }
    }
}
