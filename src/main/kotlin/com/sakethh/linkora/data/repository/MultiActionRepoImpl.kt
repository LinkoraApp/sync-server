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
import com.sakethh.linkora.domain.tables.LinkTagTable
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
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

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
            lateinit var globalLinksOldToNewIdsMap: Map<Long, Long>
            val copiedFolderResponse = mutableListOf<CopiedFolderResponse>()
            transaction {
                val sourceGlobalSelectedLinks = LinksTable.selectAll().where {
                    LinksTable.id inList copyItemsDTO.linkIds.values
                }.toList()

                globalLinksOldToNewIdsMap = LinksTable.copy(
                    source = sourceGlobalSelectedLinks,
                    eventTimestamp = eventTimestamp,
                    parentFolderId = copyItemsDTO.newParentFolderId,
                    newLinkType = copyItemsDTO.linkType
                )

                insertNewLinkTags(oldToNewLinkIdsMap = globalLinksOldToNewIdsMap)

                // initially, we'll insert the root folders
                val sourceRootFolders = FoldersTable.selectAll().where {
                    FoldersTable.id.inList(copyItemsDTO.folders.map { it.currentFolder.remoteId })
                }.toList()

                val copiedRootFolderOldToNewIdsMap = FoldersTable.copy(
                    source = sourceRootFolders,
                    eventTimestamp = eventTimestamp,
                    parentFolderId = copyItemsDTO.newParentFolderId
                )

                copyItemsDTO.folders.forEach { (currentFolder, _, _) ->
                    copiedFolderResponse.add(
                        CopiedFolderResponse(
                            currentFolder = CurrentFolder(
                                localId = currentFolder.localId,
                                remoteId = copiedRootFolderOldToNewIdsMap.getValue(currentFolder.remoteId)
                            ), links = emptyList()
                        )
                    )
                }

                // insert links of root folders
                copyItemsDTO.folders.forEachIndexed { folderIndex, folder ->
                    val sourceLinksOfRootFolder = LinksTable.selectAll().where {
                        LinksTable.id.inList(folder.links.map { it.remoteId })
                    }.toList()

                    val newRootFolderLinkIds = LinksTable.copy(
                        source = sourceLinksOfRootFolder,
                        eventTimestamp = eventTimestamp,
                        parentFolderId = copiedRootFolderOldToNewIdsMap[folder.currentFolder.remoteId]
                    )

                    insertNewLinkTags(oldToNewLinkIdsMap = newRootFolderLinkIds)

                    val folderLinks = folder.links.mapNotNull {
                        val newRemoteId = newRootFolderLinkIds[it.remoteId]
                        if (newRemoteId != null) {
                            FolderLink(localId = it.localId, remoteId = newRemoteId)
                        } else {
                            println("folder.links.mapNotNull hit null")
                            null
                        }
                    }
                    copiedFolderResponse[folderIndex] = copiedFolderResponse[folderIndex].copy(
                        links = folderLinks
                    )
                }

                fun insertChildFolders(
                    parentFolderId: Long,
                    childFolders: List<CopyFolderDTO>,
                ) {
                    val sourceFolders = FoldersTable.selectAll().where {
                        FoldersTable.id.inList(childFolders.map { it.currentFolder.remoteId })
                    }.toList()

                    val oldToNewChildFolderMap =
                        FoldersTable.copy(source = sourceFolders, eventTimestamp, parentFolderId)

                    childFolders.forEach { childFolder ->
                        val oldFolderId = childFolder.currentFolder.remoteId
                        val newFolderId = oldToNewChildFolderMap.getValue(oldFolderId)

                        val sourceCurrentFolderLinks = LinksTable.selectAll().where {
                            LinksTable.id.inList(childFolder.links.map { it.remoteId })
                        }
                        val newChildFolderLinksMap = LinksTable.copy(
                            source = sourceCurrentFolderLinks.toList(),
                            eventTimestamp = eventTimestamp,
                            parentFolderId = newFolderId
                        )

                        insertNewLinkTags(oldToNewLinkIdsMap = newChildFolderLinksMap)

                        val folderLinks = childFolder.links.mapNotNull { link ->
                            val newRemoteId = newChildFolderLinksMap[link.remoteId]
                            if (newRemoteId != null) {
                                FolderLink(localId = link.localId, remoteId = newRemoteId)
                            } else {
                                println("childFolder.links.mapNotNull hit null")
                                null
                            }
                        }

                        copiedFolderResponse.add(
                            CopiedFolderResponse(
                                currentFolder = CurrentFolder(
                                    localId = childFolder.currentFolder.localId, remoteId = newFolderId
                                ), links = folderLinks
                            )
                        )
                        insertChildFolders(
                            parentFolderId = newFolderId, childFolder.childFolders
                        )
                    }
                }

                // insert child folders
                copyItemsDTO.folders.forEach { parentFolder ->
                    insertChildFolders(
                        parentFolderId = copiedRootFolderOldToNewIdsMap.getValue(parentFolder.currentFolder.remoteId),
                        childFolders = parentFolder.childFolders
                    )
                }
            }
            Result.Success(
                response = CopyItemsHTTPResponseDTO(
                    folders = copiedFolderResponse.toList(),
                    linkIds = globalLinksOldToNewIdsMap,
                    correlation = copyItemsDTO.correlation,
                    eventTimestamp = eventTimestamp
                ), webSocketEvent = WebSocketEvent(
                    operation = Route.MultiAction.COPY_EXISTING_ITEMS.name, payload = Json.encodeToJsonElement(
                        CopyItemsSocketResponseDTO(
                            eventTimestamp = eventTimestamp, correlation = copyItemsDTO.correlation
                        )
                    )
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    private fun insertNewLinkTags(oldToNewLinkIdsMap: Map<Long, Long>) {
        val eventTimestamp = getSystemEpochSeconds()
        val previousAssignedTags = LinkTagTable.selectAll().where {
            LinkTagTable.linkId inList oldToNewLinkIdsMap.keys
        }.groupBy {
            it[LinkTagTable.linkId]
        }.mapValues {
            it.value.map {
                it[LinkTagTable.tagId]
            }
        }

        previousAssignedTags.forEach { (linkId, tagsIds) ->
            val newLinkId = oldToNewLinkIdsMap[linkId]
            if (newLinkId != null) {
                LinkTagTable.batchInsert(tagsIds) {
                    this[LinkTagTable.tagId] = it
                    this[LinkTagTable.linkId] = newLinkId
                    this[LinkTagTable.lastModified] = eventTimestamp
                }
            }
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