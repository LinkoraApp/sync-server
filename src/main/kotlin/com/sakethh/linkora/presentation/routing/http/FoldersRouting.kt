package com.sakethh.linkora.presentation.routing.http

import com.sakethh.linkora.Security
import com.sakethh.linkora.domain.Route
import com.sakethh.linkora.domain.dto.IDBasedDTO
import com.sakethh.linkora.domain.dto.folder.*
import com.sakethh.linkora.domain.repository.FoldersRepository
import com.sakethh.linkora.utils.respondWithResult
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.foldersRouting(foldersRepository: FoldersRepository) {
    routing {
        authenticate(Security.BEARER.name) {
            post<AddFolderDTO>(Route.Folder.CREATE_FOLDER.name) { folderDTO ->
                respondWithResult(foldersRepository.createFolder(folderDTO))
            }

            post<IDBasedDTO>(Route.Folder.DELETE_FOLDER.name) {
                respondWithResult(foldersRepository.deleteFolder(it))
            }

            post<IDBasedDTO>(Route.Folder.GET_CHILD_FOLDERS.name) {
                respondWithResult(foldersRepository.getChildFolders(it))
            }

            get(Route.Folder.GET_ROOT_FOLDERS.name) {
                respondWithResult(foldersRepository.getRootFolders())
            }

            post<IDBasedDTO>(Route.Folder.MARK_FOLDER_AS_ARCHIVE.name) {
                respondWithResult(foldersRepository.markAsArchive(it))
            }

            post<IDBasedDTO>(Route.Folder.MARK_AS_REGULAR_FOLDER.name) {
                respondWithResult(foldersRepository.markAsRegularFolder(it))
            }

            post<MoveFoldersDTO>(Route.Folder.MOVE_FOLDERS.name) {
                respondWithResult(
                    foldersRepository.moveFolders(it)
                )
            }

            post<UpdateFolderNameDTO>(Route.Folder.UPDATE_FOLDER_NAME.name) {
                respondWithResult(
                    foldersRepository.updateFolderName(it)
                )
            }

            post<UpdateFolderNoteDTO>(Route.Folder.UPDATE_FOLDER_NOTE.name) {
                respondWithResult(foldersRepository.updateFolderNote(it))
            }

            post<IDBasedDTO>(Route.Folder.DELETE_FOLDER_NOTE.name) {
                respondWithResult(foldersRepository.deleteFolderNote(it))
            }

            post<MarkSelectedFoldersAsRootDTO>(Route.Folder.MARK_FOLDERS_AS_ROOT.name) {
                respondWithResult(foldersRepository.markSelectedFoldersAsRoot(it))
            }
        }
    }
}
