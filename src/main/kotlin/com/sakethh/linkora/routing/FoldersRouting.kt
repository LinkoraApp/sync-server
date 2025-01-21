package com.sakethh.linkora.routing

import com.sakethh.linkora.Security
import com.sakethh.linkora.domain.dto.folder.ChangeParentFolderDTO
import com.sakethh.linkora.domain.dto.folder.AddFolderDTO
import com.sakethh.linkora.domain.dto.folder.UpdateFolderNameDTO
import com.sakethh.linkora.domain.dto.folder.UpdateFolderNoteDTO
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

            post<Long>(FolderRoute.DELETE_FOLDER.name) { folderId ->
                respondWithResult(foldersRepository.deleteFolder(folderId))
            }

            post<Long>(FolderRoute.GET_CHILD_FOLDERS.name) { folderId ->
                respondWithResult(foldersRepository.getChildFolders(folderId))
            }

            get(FolderRoute.GET_ROOT_FOLDERS.name) {
                respondWithResult(foldersRepository.getRootFolders())
            }

            post<Long>(FolderRoute.MARK_AS_ARCHIVE.name) { folderId ->
                respondWithResult(foldersRepository.markAsArchive(folderId))
            }

            post<Long>(FolderRoute.MARK_AS_REGULAR_FOLDER.name) { folderId ->
                respondWithResult(foldersRepository.markAsRegularFolder(folderId))
            }

            post<ChangeParentFolderDTO>(FolderRoute.CHANGE_PARENT_FOLDER.name) {
                respondWithResult(
                    foldersRepository.changeParentFolder(
                        folderId = it.folderId, newParentFolderId = it.newParentFolderId
                    )
                )
            }

            post<UpdateFolderNameDTO>(FolderRoute.UPDATE_FOLDER_NAME.name) {
                respondWithResult(
                    foldersRepository.updateFolderName(
                        folderId = it.folderId, newFolderName = it.newFolderName
                    )
                )
            }

            post<UpdateFolderNoteDTO>(FolderRoute.UPDATE_FOLDER_NOTE.name) {
                respondWithResult(foldersRepository.updateFolderNote(folderId = it.folderId, newNote = it.newNote))
            }

            post<Long>(FolderRoute.DELETE_FOLDER_NOTE.name) { folderId ->
                respondWithResult(foldersRepository.deleteFolderNote(folderId = folderId))
            }
        }
    }
}
