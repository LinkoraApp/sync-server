package com.sakethh.linkora.routing

import com.sakethh.linkora.Security
import com.sakethh.linkora.domain.dto.IDBasedDTO
import com.sakethh.linkora.domain.dto.panel.AddANewPanelDTO
import com.sakethh.linkora.domain.dto.panel.AddANewPanelFolderDTO
import com.sakethh.linkora.domain.dto.panel.DeleteAPanelFromAFolderDTO
import com.sakethh.linkora.domain.dto.panel.UpdatePanelNameDTO
import com.sakethh.linkora.domain.repository.PanelsRepository
import com.sakethh.linkora.domain.routes.PanelRoute
import com.sakethh.linkora.utils.respondWithResult
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.panelsRouting(panelsRepository: PanelsRepository) {
    routing {
        authenticate(Security.BEARER.name) {
            post<AddANewPanelDTO>(PanelRoute.ADD_A_NEW_PANEL.name) {
                respondWithResult(panelsRepository.addANewPanel(it))
            }

            post<AddANewPanelFolderDTO>(PanelRoute.ADD_A_NEW_FOLDER_IN_A_PANEL.name) {
                respondWithResult(panelsRepository.addANewFolderInAPanel(it))
            }

            post<IDBasedDTO>(PanelRoute.DELETE_A_PANEL.name) {
                respondWithResult(panelsRepository.deleteAPanel(it))
            }

            post<UpdatePanelNameDTO>(PanelRoute.UPDATE_A_PANEL_NAME.name) {
                respondWithResult(panelsRepository.updateAPanelName(it))
            }

            post<IDBasedDTO>(PanelRoute.DELETE_A_FOLDER_FROM_ALL_PANELS.name) {
                respondWithResult(panelsRepository.deleteAFolderFromAllPanels(it))
            }

            post<DeleteAPanelFromAFolderDTO>(PanelRoute.DELETE_A_FOLDER_FROM_A_PANEL.name) {
                respondWithResult(panelsRepository.deleteAFolderFromAPanel(it))
            }

            post<IDBasedDTO>(PanelRoute.DELETE_ALL_FOLDERS_FROM_A_PANEL.name) {
                respondWithResult(panelsRepository.deleteAllFoldersFromAPanel(it))
            }
        }
    }
}