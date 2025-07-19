package com.sakethh.linkora.data.repository

import com.sakethh.linkora.data.linkoraTables
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.MediaType
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.Route
import com.sakethh.linkora.domain.dto.AllTablesDTO
import com.sakethh.linkora.domain.dto.DeleteEverythingDTO
import com.sakethh.linkora.domain.dto.Tombstone
import com.sakethh.linkora.domain.model.*
import com.sakethh.linkora.domain.repository.SyncRepo
import com.sakethh.linkora.domain.tables.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.Instant

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
                            mediaType = MediaType.valueOf(it[LinksTable.mediaType]),
                            eventTimestamp = it[LinksTable.lastModified]
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
                        Panel(
                            panelId = it[PanelsTable.id].value,
                            panelName = it[PanelsTable.panelName],
                            eventTimestamp = it[PanelsTable.lastModified]
                        )
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
                            connectedPanelId = it[PanelFoldersTable.connectedPanelId],
                            eventTimestamp = it[PanelFoldersTable.lastModified]
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
                            isArchived = it[FoldersTable.isFolderArchived],
                            eventTimestamp = it[FoldersTable.lastModified]
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

    override suspend fun deleteEverything(deleteEverythingDTO: DeleteEverythingDTO): Result<Unit> {
        return try {
            val eventTimestamp = Instant.now().epochSecond
            transaction {
                linkoraTables().forEach {
                    it.deleteAll()
                }
            }
            Result.Success(
                response = Unit, webSocketEvent = WebSocketEvent(
                    operation = Route.Sync.DELETE_EVERYTHING.name,
                    payload = Json.encodeToJsonElement(deleteEverythingDTO.copy(eventTimestamp = eventTimestamp))
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Failure(e)
        }
    }
}