package com.sakethh.linkora.routing

import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.dto.nonSavedLinksAndNonFolderLinks.*
import com.sakethh.linkora.domain.repository.NonSavedAndNonFolderLinksRepository
import com.sakethh.linkora.domain.routes.NonSavedAndNonFolderLinkRoute
import com.sakethh.linkora.utils.respondWithResult
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.nonSavedAndNonFolderLinksRouting(nonSavedAndNonFolderLinksRepository: NonSavedAndNonFolderLinksRepository) {
    routing {
        post<NonSavedAndNonFolderLinkDTO>(NonSavedAndNonFolderLinkRoute.CREATE_A_NEW_LINK.name) {
            respondWithResult(
                nonSavedAndNonFolderLinksRepository.createANewLink(
                    linkType = it.linkType, nonSavedAndNonFolderLinkDTO = it
                )
            )
        }
        post<DeleteANonSavedAndNonFolderLinkDTO>(NonSavedAndNonFolderLinkRoute.DELETE_A_NEW_LINK.name) {
            respondWithResult(
                nonSavedAndNonFolderLinksRepository.deleteANewLink(
                    linkType = it.linkType, linkId = it.linkId
                )
            )
        }
        post<LinkType>(NonSavedAndNonFolderLinkRoute.GET_ALL_LINKS.name) {
            respondWithResult(
                nonSavedAndNonFolderLinksRepository.getAllLinks(linkType = it)
            )
        }

        post<UpdateANonSavedAndNonFolderLinkTitleDTO>(NonSavedAndNonFolderLinkRoute.UPDATE_LINK_TITLE.name) {
            respondWithResult(
                nonSavedAndNonFolderLinksRepository.updateLinkTitle(
                    linkType = it.linkType, linkId = it.linkId, newTitle = it.newTitle
                )
            )
        }

        post<UpdateANonSavedAndNonFolderLinkNoteDTO>(NonSavedAndNonFolderLinkRoute.UPDATE_LINK_NOTE.name) {
            respondWithResult(
                nonSavedAndNonFolderLinksRepository.updateLinkTitle(
                    linkType = it.linkType, linkId = it.linkId, newTitle = it.newNote
                )
            )
        }

        post<DeleteANonSavedAndNonFolderLinkNoteDTO>(NonSavedAndNonFolderLinkRoute.DELETE_LINK_NOTE.name) {
            respondWithResult(
                nonSavedAndNonFolderLinksRepository.deleteLinkNote(linkType = it.linkType, linkId = it.linkId)
            )
        }

    }
}