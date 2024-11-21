package com.sakethh.linkora.domain.mapper

import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.dto.link.LinkDTO
import com.sakethh.linkora.domain.tables.LinksTable
import org.jetbrains.exposed.sql.Query

class LinksMapper {
    fun toDto(query: Query): List<LinkDTO> {
        return query.map { resultRow ->
            LinkDTO(
                id = resultRow[LinksTable.id],
                linkTitle = resultRow[LinksTable.linkTitle],
                webURL = resultRow[LinksTable.webURL],
                baseURL = resultRow[LinksTable.baseURL],
                imgURL = resultRow[LinksTable.imgURL],
                infoForSaving = resultRow[LinksTable.infoForSaving],
                isLinkedWithSavedLinks = resultRow[LinksTable.isLinkedWithSavedLinks],
                isLinkedWithFolders = resultRow[LinksTable.isLinkedWithFolders],
                idOfLinkedFolder = resultRow[LinksTable.idOfLinkedFolder],
                userAgent = resultRow[LinksTable.userAgent],
                linkType = LinkType.valueOf(resultRow[LinksTable.linkType]),
                lastModified = resultRow[LinksTable.lastModified]
            )
        }
    }
}