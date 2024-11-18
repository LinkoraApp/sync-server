package com.sakethh.linkora.domain.mapper

import com.sakethh.linkora.domain.dto.SavedAndFolderLinksDTO
import com.sakethh.linkora.domain.tables.SavedAndFolderLinksTable
import org.jetbrains.exposed.sql.Query

class SavedFolderLinksMapper {
    fun toDto(query: Query): List<SavedAndFolderLinksDTO> {
        return query.map { resultRow ->
            SavedAndFolderLinksDTO(
                id = resultRow[SavedAndFolderLinksTable.id].value,
                linkTitle = resultRow[SavedAndFolderLinksTable.linkTitle],
                webURL = resultRow[SavedAndFolderLinksTable.webURL],
                baseURL = resultRow[SavedAndFolderLinksTable.baseURL],
                imgURL = resultRow[SavedAndFolderLinksTable.imgURL],
                infoForSaving = resultRow[SavedAndFolderLinksTable.infoForSaving],
                isLinkedWithSavedLinks = resultRow[SavedAndFolderLinksTable.isLinkedWithSavedLinks],
                isLinkedWithFolders = resultRow[SavedAndFolderLinksTable.isLinkedWithFolders],
                idOfLinkedFolder = resultRow[SavedAndFolderLinksTable.idOfLinkedFolder],
                userAgent = resultRow[SavedAndFolderLinksTable.userAgent]
            )
        }
    }
}