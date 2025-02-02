package com.sakethh.linkora.data.repository

import com.sakethh.linkora.data.linkoraTables
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.MediaType
import com.sakethh.linkora.domain.dto.AllTablesDTO
import com.sakethh.linkora.domain.dto.Tombstone
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.Link
import com.sakethh.linkora.domain.model.Panel
import com.sakethh.linkora.domain.model.PanelFolder
import com.sakethh.linkora.domain.repository.SyncRepo
import com.sakethh.linkora.domain.tables.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class SyncRepoImpl : SyncRepo {
    override suspend fun getTombstonesAfter(eventTimestamp: Long): List<Tombstone> {
        return transaction {
            TombstoneTable.selectAll().where {
                TombstoneTable.deletedAt.greater(eventTimestamp)
            }.toList().map {
                Tombstone(
                    deletedAt = it[TombstoneTable.deletedAt],
                    operation = it[TombstoneTable.operation],
                    payload = Json.parseToJsonElement(it[TombstoneTable.payload])
                )
            }
        }
    }

    override suspend fun getUpdatesAfter(eventTimestamp: Long): AllTablesDTO = coroutineScope {
        val updatedLinks = mutableListOf<Link>()
        val updatedFolders = mutableListOf<Folder>()
        val updatedPanels = mutableListOf<Panel>()
        val updatedPanelFolders = mutableListOf<PanelFolder>()

        awaitAll(async {
            transaction {
                LinksTable.selectAll().where {
                    LinksTable.lastModified.greater(eventTimestamp)
                }.toList().forEach {
                    updatedLinks.add(
                        Link(
                            id = it[LinksTable.id].value,
                            linkType = LinkType.valueOf(it[LinksTable.linkType]),
                            title = it[LinksTable.linkTitle],
                            url = it[LinksTable.url],
                            baseURL = it[LinksTable.baseURL],
                            imgURL = it[LinksTable.imgURL],
                            note = it[LinksTable.note],
                            idOfLinkedFolder = it[LinksTable.idOfLinkedFolder],
                            userAgent = it[LinksTable.userAgent],
                            markedAsImportant = it[LinksTable.markedAsImportant],
                            mediaType = MediaType.valueOf(it[LinksTable.mediaType])
                        )
                    )
                }
            }
        }, async {
            transaction {
                PanelsTable.selectAll().where {
                    PanelsTable.lastModified.greater(eventTimestamp)
                }.toList().forEach {
                    updatedPanels.add(
                        Panel(panelId = it[PanelsTable.id].value, panelName = it[PanelsTable.panelName])
                    )
                }
            }
        }, async {
            transaction {
                PanelFoldersTable.selectAll().where {
                    PanelFoldersTable.lastModified.greater(eventTimestamp)
                }.toList().forEach {
                    updatedPanelFolders.add(
                        PanelFolder(
                            id = it[PanelFoldersTable.id].value,
                            folderId = it[PanelFoldersTable.folderId],
                            panelPosition = it[PanelFoldersTable.panelPosition],
                            folderName = it[PanelFoldersTable.folderName],
                            connectedPanelId = it[PanelFoldersTable.connectedPanelId]
                        )
                    )
                }
            }
        }, async {
            transaction {
                FoldersTable.selectAll().where {
                    FoldersTable.lastModified.greater(eventTimestamp)
                }.toList().forEach {
                    updatedFolders.add(
                        Folder(
                            id = it[FoldersTable.id].value,
                            name = it[FoldersTable.folderName],
                            note = it[FoldersTable.note],
                            parentFolderId = it[FoldersTable.parentFolderID],
                            isArchived = it[FoldersTable.isFolderArchived]
                        )
                    )
                }
            }
        })

        return@coroutineScope AllTablesDTO(
            links = updatedLinks.toList(),
            folders = updatedFolders.toList(),
            panels = updatedPanels.toList(),
            panelFolders = updatedPanelFolders.toList()
        )
    }

    override suspend fun deleteEverything(): Result<Unit> {
        return try {
            transaction {
                linkoraTables().forEach {
                    it.deleteAll()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}