package com.sakethh.linkora.data.repository

import com.sakethh.linkora.domain.DeleteMultipleItemsDTO
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.Route
import com.sakethh.linkora.domain.dto.*
import com.sakethh.linkora.domain.dto.folder.MarkItemsRegularDTO
import com.sakethh.linkora.domain.model.WebSocketEvent
import com.sakethh.linkora.domain.repository.FoldersRepo
import com.sakethh.linkora.domain.repository.MultiActionRepo
import com.sakethh.linkora.domain.tables.FoldersTable
import com.sakethh.linkora.domain.tables.LinksTable
import com.sakethh.linkora.domain.tables.helper.TombStoneHelper
import com.sakethh.linkora.utils.checkForLWWConflictAndThrow
import com.sakethh.linkora.utils.copy
import com.sakethh.linkora.utils.getSystemEpochSeconds
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.inList
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.Instant

class MultiActionRepoImpl(
    private val foldersRepo: FoldersRepo
) : MultiActionRepo {
    override suspend fun archiveMultipleItems(archiveMultipleItemsDTO: ArchiveMultipleItemsDTO): Result<TimeStampBasedResponse> {
        return try {
            if (archiveMultipleItemsDTO.linkIds.isNotEmpty()) {
                LinksTable.checkForLWWConflictAndThrow(
                    id = archiveMultipleItemsDTO.linkIds.last(),
                    timeStamp = archiveMultipleItemsDTO.eventTimestamp,
                    lastModifiedColumn = LinksTable.lastModified
                )
            }
            if (archiveMultipleItemsDTO.folderIds.isNotEmpty()) {
                FoldersTable.checkForLWWConflictAndThrow(
                    id = archiveMultipleItemsDTO.folderIds.last(),
                    timeStamp = archiveMultipleItemsDTO.eventTimestamp,
                    lastModifiedColumn = FoldersTable.lastModified
                )
            }
            val eventTimestamp = getSystemEpochSeconds()
            var updatedRowsCount = 0
            coroutineScope {
                awaitAll(async {
                    transaction {
                        updatedRowsCount += LinksTable.update(where = {
                            LinksTable.id.inList(archiveMultipleItemsDTO.linkIds)
                        }) {
                            it[lastModified] = eventTimestamp
                            it[linkType] = LinkType.ARCHIVE_LINK.name
                        }
                    }
                }, async {
                    transaction {
                        updatedRowsCount += FoldersTable.update(where = {
                            FoldersTable.id.inList(archiveMultipleItemsDTO.folderIds)
                        }) {
                            it[lastModified] = eventTimestamp
                            it[isFolderArchived] = true
                        }
                    }
                })
            }
            Result.Success(
                response = TimeStampBasedResponse(
                    eventTimestamp = eventTimestamp, message = "Archived $updatedRowsCount items."
                ), webSocketEvent = WebSocketEvent(
                    operation = Route.MultiAction.ARCHIVE_MULTIPLE_ITEMS.name,
                    payload = Json.encodeToJsonElement(archiveMultipleItemsDTO.copy(eventTimestamp = eventTimestamp))
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun deleteMultipleItems(deleteMultipleItemsDTO: DeleteMultipleItemsDTO): Result<TimeStampBasedResponse> {
        val eventTimestamp = getSystemEpochSeconds()
        return try {
            deleteMultipleItemsDTO.folderIds.forEach {
                foldersRepo.deleteFolder(
                    IDBasedDTO(
                        id = it,
                        correlation = deleteMultipleItemsDTO.correlation,
                        eventTimestamp = deleteMultipleItemsDTO.eventTimestamp
                    )
                )
            }
            transaction {
                TombStoneHelper.insert(
                    payload = Json.encodeToString(deleteMultipleItemsDTO.copy(eventTimestamp = eventTimestamp)),
                    operation = Route.MultiAction.DELETE_MULTIPLE_ITEMS.name,
                    deletedAt = eventTimestamp
                )

                LinksTable.deleteWhere {
                    LinksTable.id.inList(deleteMultipleItemsDTO.linkIds)
                }
            }
            Result.Success(
                response = TimeStampBasedResponse(
                    eventTimestamp = eventTimestamp, message = "Deleted."
                ), webSocketEvent = WebSocketEvent(
                    operation = Route.MultiAction.DELETE_MULTIPLE_ITEMS.name,
                    payload = Json.encodeToJsonElement(deleteMultipleItemsDTO.copy(eventTimestamp = eventTimestamp))
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun moveMultipleItems(moveItemsDTO: MoveItemsDTO): Result<TimeStampBasedResponse> {
        return try {
            if (moveItemsDTO.folderIds.isNotEmpty()) {
                FoldersTable.checkForLWWConflictAndThrow(
                    id = moveItemsDTO.folderIds.last(),
                    timeStamp = moveItemsDTO.eventTimestamp,
                    lastModifiedColumn = FoldersTable.lastModified
                )
            }
            if (moveItemsDTO.linkIds.isNotEmpty()) {
                LinksTable.checkForLWWConflictAndThrow(
                    id = moveItemsDTO.linkIds.last(),
                    timeStamp = moveItemsDTO.eventTimestamp,
                    lastModifiedColumn = LinksTable.lastModified
                )
            }
            val eventTimestamp = getSystemEpochSeconds()
            var rowsUpdated = 0
            transaction {
                rowsUpdated += LinksTable.update(where = {
                    LinksTable.id.inList(moveItemsDTO.linkIds)
                }) {
                    it[lastModified] = eventTimestamp
                    it[idOfLinkedFolder] = moveItemsDTO.newParentFolderId
                    it[linkType] = moveItemsDTO.linkType.name
                }
                rowsUpdated += FoldersTable.update(where = { FoldersTable.id.inList(moveItemsDTO.folderIds) }) {
                    it[lastModified] = eventTimestamp
                    it[parentFolderID] = moveItemsDTO.newParentFolderId
                }
            }
            Result.Success(
                response = TimeStampBasedResponse(
                    message = "Number of rows affected by the update = $rowsUpdated", eventTimestamp = eventTimestamp
                ), webSocketEvent = WebSocketEvent(
                    operation = Route.MultiAction.MOVE_EXISTING_ITEMS.name,
                    payload = Json.encodeToJsonElement(
                        moveItemsDTO.copy(
                            eventTimestamp = eventTimestamp
                        )
                    ),
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun copyMultipleItems(copyItemsDTO: CopyItemsDTO): Result<CopyItemsHTTPResponseDTO> {
        return try {
            val eventTimestamp = getSystemEpochSeconds()
            lateinit var linkIds: List<Long>
            println("Remote Links : ${copyItemsDTO.linkIds.values}")
            // copy the links based on `copyItemsDTO.linkIds`
            val copiedFolderResponse = mutableListOf<CopiedFolderResponse>()
            transaction {
                this.exec(
                    """
                   INSERT INTO links_table (last_modified,link_type,link_title,url,base_url,img_url,note,id_of_linked_folder,user_agent,media_type,marked_as_important)
                   SELECT $eventTimestamp AS last_modified,'${copyItemsDTO.linkType.name}' AS link_type,link_title,url,base_url,img_url,note,${copyItemsDTO.newParentFolderId} AS id_of_linked_folder,user_agent,media_type,marked_as_important FROM links_table
                   WHERE id IN (${copyItemsDTO.linkIds.values.joinToString(separator = ",")});
               """.trimIndent()
                )
                linkIds = LinksTable.select(LinksTable.id).where {
                    LinksTable.lastModified.eq(eventTimestamp) and LinksTable.linkType.eq(copyItemsDTO.linkType.name) and LinksTable.idOfLinkedFolder.eq(
                        copyItemsDTO.newParentFolderId
                    )
                }.map { resultRow ->
                    resultRow[LinksTable.id].value
                }.toList()


                // initially, we'll insert the root folders
                lateinit var copiedRootFolderIds: List<Long>
                val sourceRootFolders = FoldersTable.selectAll().where {
                    FoldersTable.id.inList(copyItemsDTO.folders.map { it.currentFolder.remoteId })
                }.toList()
                val eventTimestamp = getSystemEpochSeconds()

                copiedRootFolderIds = FoldersTable.copy(
                    source = sourceRootFolders,
                    eventTimestamp = eventTimestamp,
                    parentFolderId = copyItemsDTO.newParentFolderId
                ).mapIndexed { index, resultRow ->
                    resultRow[FoldersTable.id].value.run {
                        copiedFolderResponse.add(
                            CopiedFolderResponse(
                                currentFolder = CurrentFolder(
                                    localId = copyItemsDTO.folders[index].currentFolder.localId, remoteId = this
                                ), links = emptyList()
                            )
                        )
                        this
                    }
                }

                // insert links of root folders
                copyItemsDTO.folders.map { it.currentFolder.remoteId }.forEachIndexed { folderIndex, folderId ->
                    val sourceLinksOfRootFolder = LinksTable.selectAll().where {
                        LinksTable.idOfLinkedFolder.eq(folderId)
                    }.toList()
                    val eventTimestamp = getSystemEpochSeconds()
                    LinksTable.copy(
                        source = sourceLinksOfRootFolder,
                        eventTimestamp = eventTimestamp,
                        parentFolderId = copiedRootFolderIds[folderIndex]
                    ).mapIndexed { linkResultRowIndex, resultRow ->
                        FolderLink(
                            localId = copyItemsDTO.folders[folderIndex].links[linkResultRowIndex].localId,
                            remoteId = resultRow[LinksTable.id].value
                        )
                    }.let {
                        copiedFolderResponse[folderIndex] = copiedFolderResponse[folderIndex].copy(
                            links = it
                        )
                    }
                }

                fun insertChildFolders(
                    parentFolderId: Long,
                    childFolders: List<CopyFolderDTO>,
                ) {
                    val sourceFolders = FoldersTable.selectAll().where {
                        FoldersTable.id.inList(childFolders.map { it.currentFolder.remoteId })
                    }.toList()
                    val eventTimestamp = getSystemEpochSeconds()
                    FoldersTable.copy(source = sourceFolders, eventTimestamp, parentFolderId)
                        .forEachIndexed { insertedFolderRowIndex, insertedFolderRow ->
                            val newParentFolderId = insertedFolderRow[FoldersTable.id].value
                            val sourceParentFolderId = sourceFolders[insertedFolderRowIndex][FoldersTable.id].value

                            LinksTable.selectAll().where {
                                LinksTable.idOfLinkedFolder.eq(sourceParentFolderId)
                            }.let {
                                LinksTable.copy(
                                    source = it.toList(),
                                    eventTimestamp = eventTimestamp,
                                    parentFolderId = newParentFolderId
                                ).mapIndexed { insertedLinkRowIndex, insertedLinkRow ->
                                    FolderLink(
                                        localId = childFolders[insertedFolderRowIndex].links[insertedLinkRowIndex].localId,
                                        remoteId = insertedLinkRow[LinksTable.id].value
                                    )
                                }.let {
                                    copiedFolderResponse.add(
                                        CopiedFolderResponse(
                                            currentFolder = CurrentFolder(
                                                localId = childFolders[insertedFolderRowIndex].currentFolder.localId,
                                                remoteId = newParentFolderId
                                            ), links = it
                                        )
                                    )
                                }
                            }
                            insertChildFolders(
                                newParentFolderId, childFolders[insertedFolderRowIndex].childFolders
                            )
                        }
                }

                // insert child folders
                copyItemsDTO.folders.forEachIndexed { index, parentFolder ->
                    insertChildFolders(
                        copiedRootFolderIds[index], parentFolder.childFolders
                    )
                }

            }.let {
                Result.Success(
                    response = CopyItemsHTTPResponseDTO(
                        folders = copiedFolderResponse.toList(), linkIds = copyItemsDTO.linkIds.run {
                            this.toList().mapIndexed { index, pair ->
                                pair.first to linkIds[index]
                            }.toMap()
                        }, correlation = copyItemsDTO.correlation, eventTimestamp = eventTimestamp
                    ), webSocketEvent = WebSocketEvent(
                        operation = Route.MultiAction.COPY_EXISTING_ITEMS.name, payload = Json.encodeToJsonElement(
                            CopyItemsSocketResponseDTO(
                                eventTimestamp = eventTimestamp, correlation = copyItemsDTO.correlation
                            )
                        )
                    )
                )
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun markItemsAsRegular(markItemsRegularDTO: MarkItemsRegularDTO): Result<TimeStampBasedResponse> {
        return try {
            val eventTimestamp = getSystemEpochSeconds()
            if (markItemsRegularDTO.foldersIds.isNotEmpty()) {
                FoldersTable.checkForLWWConflictAndThrow(
                    id = markItemsRegularDTO.foldersIds.random(),
                    timeStamp = markItemsRegularDTO.eventTimestamp,
                    lastModifiedColumn = FoldersTable.lastModified
                )
            }
            if (markItemsRegularDTO.linkIds.isNotEmpty()) {
                LinksTable.checkForLWWConflictAndThrow(
                    id = markItemsRegularDTO.linkIds.random(),
                    timeStamp = markItemsRegularDTO.eventTimestamp,
                    lastModifiedColumn = LinksTable.lastModified
                )
            }
            transaction {
                FoldersTable.update(where = {
                    FoldersTable.id.inList(markItemsRegularDTO.foldersIds)
                }) {
                    it[isFolderArchived] = false
                    it[lastModified] = eventTimestamp
                }
                +LinksTable.update(where = {
                    LinksTable.id.inList(markItemsRegularDTO.linkIds)
                }) {
                    it[linkType] = LinkType.SAVED_LINK.name
                    it[lastModified] = eventTimestamp
                }
            }.let {
                Result.Success(
                    response = TimeStampBasedResponse(
                        eventTimestamp = eventTimestamp, message = "Unarchived $it items."
                    ), webSocketEvent = WebSocketEvent(
                        operation = Route.MultiAction.UNARCHIVE_MULTIPLE_ITEMS.name,
                        payload = Json.encodeToJsonElement(markItemsRegularDTO.copy(eventTimestamp = eventTimestamp))
                    )
                )
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
}