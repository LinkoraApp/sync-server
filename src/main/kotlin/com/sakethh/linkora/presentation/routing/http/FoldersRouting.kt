package com.sakethh.linkora.presentation.routing.http

import com.sakethh.linkora.Security
import com.sakethh.linkora.domain.dto.IDBasedDTO
import com.sakethh.linkora.domain.dto.folder.*
import com.sakethh.linkora.domain.repository.FoldersRepository
import com.sakethh.linkora.domain.routes.FolderRoute
import com.sakethh.linkora.utils.respondWithResult
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*


fun Application.foldersRouting(foldersRepository: FoldersRepository) {
    routing {
        authenticate(Security.BEARER.name) {
            post<AddFolderDTO>(FolderRoute.CREATE_FOLDER.name) { folderDTO ->
                respondWithResult(foldersRepository.createFolder(folderDTO))
            }

            post<IDBasedDTO>(FolderRoute.DELETE_FOLDER.name) {
                respondWithResult(foldersRepository.deleteFolder(it))
            }

            post<IDBasedDTO>(FolderRoute.GET_CHILD_FOLDERS.name) {
                respondWithResult(foldersRepository.getChildFolders(it))
            }

            get(FolderRoute.GET_ROOT_FOLDERS.name) {
                respondWithResult(foldersRepository.getRootFolders())
            }

            post<IDBasedDTO>(FolderRoute.MARK_FOLDER_AS_ARCHIVE.name) {
                respondWithResult(foldersRepository.markAsArchive(it))
            }

            post<IDBasedDTO>(FolderRoute.MARK_AS_REGULAR_FOLDER.name) {
                respondWithResult(foldersRepository.markAsRegularFolder(it))
            }

            post<MoveFoldersDTO>(FolderRoute.MOVE_FOLDERS.name) {
                respondWithResult(
                    foldersRepository.moveFolders(it)
                )
            }

            post<UpdateFolderNameDTO>(FolderRoute.UPDATE_FOLDER_NAME.name) {
                respondWithResult(
                    foldersRepository.updateFolderName(it)
                )
            }

            post<UpdateFolderNoteDTO>(FolderRoute.UPDATE_FOLDER_NOTE.name) {
                respondWithResult(foldersRepository.updateFolderNote(it))
            }

            post<IDBasedDTO>(FolderRoute.DELETE_FOLDER_NOTE.name) {
                respondWithResult(foldersRepository.deleteFolderNote(it))
            }

            post<MarkSelectedFoldersAsRootDTO>(FolderRoute.MARK_FOLDERS_AS_ROOT.name) {
                respondWithResult(foldersRepository.markSelectedFoldersAsRoot(it))
            }
        }
    }
}
