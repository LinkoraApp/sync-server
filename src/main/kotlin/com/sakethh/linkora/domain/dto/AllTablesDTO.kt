package com.sakethh.linkora.domain.dto

import com.sakethh.linkora.domain.Folder
import com.sakethh.linkora.domain.Link
import com.sakethh.linkora.domain.Panel
import com.sakethh.linkora.domain.PanelFolder
import kotlinx.serialization.Serializable

@Serializable
data class AllTablesDTO(
    val links: List<Link>,
    val folders: List<Folder>,
    val panels: List<Panel>,
    val panelFolders: List<PanelFolder>
)
