package com.sakethh.linkora.domain.mapper

import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.MediaType
import com.sakethh.linkora.domain.dto.link.AddLinkDTO
import com.sakethh.linkora.domain.tables.LinksTable
import org.jetbrains.exposed.sql.Query

class LinksMapper {
    fun toDto(query: Query): List<AddLinkDTO> {
        return query.map { resultRow ->
            AddLinkDTO(
                title = resultRow[LinksTable.linkTitle],
                url = resultRow[LinksTable.url],
                baseURL = resultRow[LinksTable.baseURL],
                imgURL = resultRow[LinksTable.imgURL],
                note = resultRow[LinksTable.note],
                idOfLinkedFolder = resultRow[LinksTable.idOfLinkedFolder],
                userAgent = resultRow[LinksTable.userAgent],
                linkType = LinkType.valueOf(resultRow[LinksTable.linkType]),
                lastModified = resultRow[LinksTable.lastModified],
                mediaType = MediaType.valueOf(resultRow[LinksTable.mediaType]),
                markedAsImportant = resultRow[LinksTable.markedAsImportant]
            )
        }
    }
}