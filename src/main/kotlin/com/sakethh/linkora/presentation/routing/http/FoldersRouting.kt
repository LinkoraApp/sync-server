package com.sakethh.linkora.presentation.routing.http

import com.sakethh.linkora.authenticate
import com.sakethh.linkora.domain.Route
import com.sakethh.linkora.domain.dto.IDBasedDTO
import com.sakethh.linkora.domain.dto.MoveItemsDTO
import com.sakethh.linkora.domain.dto.folder.*
import com.sakethh.linkora.domain.repository.FoldersRepo
import com.sakethh.linkora.utils.respondWithResult
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.foldersRouting(foldersRepo: FoldersRepo) {
    routing {
        authenticate {
            post<AddFolderDTO>(Route.Folder.CREATE_FOLDER.name) { folderDTO ->
                respondWithResult(foldersRepo.createFolder(folderDTO))
            }

            post<IDBasedDTO>(Route.Folder.DELETE_FOLDER.name) {
                respondWithResult(foldersRepo.deleteFolder(it))
            }

            post<IDBasedDTO>(Route.Folder.GET_CHILD_FOLDERS.name) {
                respondWithResult(foldersRepo.getChildFolders(it))
            }

            get(Route.Folder.GET_ROOT_FOLDERS.name) {
                respondWithResult(foldersRepo.getRootFolders())
            }

            post<IDBasedDTO>(Route.Folder.MARK_FOLDER_AS_ARCHIVE.name) {
                respondWithResult(foldersRepo.markAsArchive(it))
            }

            post<IDBasedDTO>(Route.Folder.MARK_AS_REGULAR_FOLDER.name) {
                respondWithResult(foldersRepo.markAsRegularFolder(it))
            }

            post<UpdateFolderNameDTO>(Route.Folder.UPDATE_FOLDER_NAME.name) {
                respondWithResult(
                    foldersRepo.updateFolderName(it)
                )
            }

            post<UpdateFolderNoteDTO>(Route.Folder.UPDATE_FOLDER_NOTE.name) {
                respondWithResult(foldersRepo.updateFolderNote(it))
            }

            post<IDBasedDTO>(Route.Folder.DELETE_FOLDER_NOTE.name) {
                respondWithResult(foldersRepo.deleteFolderNote(it))
            }

            post<MarkSelectedFoldersAsRootDTO>(Route.Folder.MARK_FOLDERS_AS_ROOT.name) {
                respondWithResult(foldersRepo.markSelectedFoldersAsRoot(it))
            }
        }
    }
}
