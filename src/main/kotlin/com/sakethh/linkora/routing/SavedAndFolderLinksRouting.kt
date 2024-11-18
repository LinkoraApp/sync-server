package com.sakethh.linkora.routing

import com.sakethh.linkora.domain.dto.savedAndFolderLinks.*
import com.sakethh.linkora.domain.repository.SavedAndFolderLinksRepository
import com.sakethh.linkora.domain.routes.SavedAndFolderLinkRoute
import com.sakethh.linkora.utils.respondWithResult
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.savedAndFolderLinksRouting(savedAndFolderLinksRepository: SavedAndFolderLinksRepository) {
    routing {
        post<SavedAndFolderLinksDTO>(SavedAndFolderLinkRoute.CREATE_A_NEW_LINK.name) {
            respondWithResult(savedAndFolderLinksRepository.createANewLink(it))
        }

        post<Long>(SavedAndFolderLinkRoute.DELETE_A_LINK.name) {
            respondWithResult(savedAndFolderLinksRepository.deleteALink(it))
        }

        post<UpdateLinkedFolderIDDto>(SavedAndFolderLinkRoute.UPDATE_LINKED_FOLDER_ID.name) {
            respondWithResult(
                savedAndFolderLinksRepository.updateLinkedFolderId(
                    linkId = it.linkId,
                    newParentFolderId = it.newParentFolderId
                )
            )
        }

        post<UpdateTitleOfTheLinkDTO>(SavedAndFolderLinkRoute.UPDATE_TITLE_OF_THE_LINK.name) {
            respondWithResult(
                savedAndFolderLinksRepository.updateTitleOfTheLink(
                    linkId = it.linkId,
                    newTitleOfTheLink = it.newTitleOfTheLink
                )
            )
        }

        post<UpdateNoteOfALinkDTO>(SavedAndFolderLinkRoute.UPDATE_NOTE.name) {
            respondWithResult(
                savedAndFolderLinksRepository.updateNote(
                    linkId = it.linkId,
                    newNote = it.newNote
                )
            )
        }

        post<Long>(SavedAndFolderLinkRoute.DELETE_NOTE.name) {
            respondWithResult(
                savedAndFolderLinksRepository.deleteNote(it)
            )
        }

        post<UpdateLinkUserAgentDTO>(SavedAndFolderLinkRoute.UPDATE_USER_AGENT.name) {
            respondWithResult(
                savedAndFolderLinksRepository.updateUserAgent(linkId = it.linkId, userAgent = it.userAgent)
            )
        }

        get(SavedAndFolderLinkRoute.GET_ALL_LINKS.name) {
            respondWithResult(savedAndFolderLinksRepository.getAllLinks())
        }

        get(SavedAndFolderLinkRoute.GET_SAVED_LINKS.name) {
            respondWithResult(savedAndFolderLinksRepository.getSavedLinks())
        }

        post<Long>(SavedAndFolderLinkRoute.GET_LINKS_FROM_A_FOLDER.name) {
            respondWithResult(savedAndFolderLinksRepository.getLinksFromAFolder(it))
        }
    }
}