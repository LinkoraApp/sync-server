package com.sakethh.linkora.routing

import com.sakethh.linkora.Security
import com.sakethh.linkora.domain.dto.folder.ChangeParentFolderDTO
import com.sakethh.linkora.domain.dto.folder.FolderDTO
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
            post<FolderDTO>(FolderRoute.CREATE_FOLDER.name) { folderDTO ->
                respondWithResult(foldersRepository.createFolder(folderDTO))
            }
        }

        authenticate(Security.BEARER.name) {
            post<Long>(FolderRoute.DELETE_FOLDER.name) { folderId ->
                respondWithResult(foldersRepository.deleteFolder(folderId))
            }
        }

        authenticate(Security.BEARER.name) {
            post<Long>(FolderRoute.GET_CHILD_FOLDERS.name) { folderId ->
                respondWithResult(foldersRepository.getChildFolders(folderId))
            }
        }

        authenticate(Security.BEARER.name) {
            get(FolderRoute.GET_ROOT_FOLDERS.name) {
                respondWithResult(foldersRepository.getRootFolders())
            }
        }

        authenticate(Security.BEARER.name) {
            post<Long>(FolderRoute.MARK_AS_ARCHIVE.name) { folderId ->
                respondWithResult(foldersRepository.markAsArchive(folderId))
            }
        }

        authenticate(Security.BEARER.name) {
            post<Long>(FolderRoute.MARK_AS_REGULAR_FOLDER.name) { folderId ->
                respondWithResult(foldersRepository.markAsRegularFolder(folderId))
            }
        }

        authenticate(Security.BEARER.name) {
            post<ChangeParentFolderDTO>(FolderRoute.CHANGE_PARENT_FOLDER.name) {
                respondWithResult(
                    foldersRepository.changeParentFolder(
                        folderId = it.folderId, newParentFolderId = it.newParentFolderId
                    )
                )
            }
        }

        authenticate(Security.BEARER.name) {
            post<UpdateFolderNameDTO>(FolderRoute.UPDATE_FOLDER_NAME.name) {
                respondWithResult(
                    foldersRepository.updateFolderName(
                        folderId = it.folderId, newFolderName = it.newFolderName
                    )
                )
            }
        }

        authenticate(Security.BEARER.name) {
            post<UpdateFolderNoteDTO>(FolderRoute.UPDATE_FOLDER_NOTE.name) {
                respondWithResult(foldersRepository.updateFolderNote(folderId = it.folderId, newNote = it.newNote))
            }
        }

        authenticate(Security.BEARER.name) {
            post<Long>(FolderRoute.DELETE_FOLDER_NOTE.name) { folderId ->
                respondWithResult(foldersRepository.deleteFolderNote(folderId = folderId))
            }
        }
    }
}
