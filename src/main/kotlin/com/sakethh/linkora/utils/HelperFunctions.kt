package com.sakethh.linkora.utils

import com.sakethh.linkora.domain.LWWConflictException
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.tables.FoldersTable
import com.sakethh.linkora.domain.tables.LinksTable
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

fun useSysEnvValues(): Boolean {
    return try {
        System.getenv(SysEnvKey.LINKORA_SERVER_USE_ENV_VAL.name).toBooleanStrict()
    } catch (_: Exception) {
        false
    }
}

@OptIn(ExperimentalTime::class)
fun getSystemEpochSeconds() = Clock.System.now().epochSeconds

fun LongIdTable.checkForLWWConflictAndThrow(id: Long, timeStamp: Long, lastModifiedColumn: Column<Long>) {
    transaction {
        this@checkForLWWConflictAndThrow.select(lastModifiedColumn).where {
            this@checkForLWWConflictAndThrow.id.eq(id)
        }.let {
            try {
                val resultRow = it.single()
                if (resultRow[lastModifiedColumn] > timeStamp) {
                    throw LWWConflictException()
                }
            } catch (e: Exception) {
                if (e is LWWConflictException) {
                    throw e
                }
                e.printStackTrace()
            }
        }
    }
}

fun FoldersTable.copy(source: List<ResultRow>, eventTimestamp: Long, parentFolderId: Long?): Map<Long, Long> {
    val oldToNewIdMap = mutableMapOf<Long, Long>()
    source.forEach { sourceRow ->
        val oldId = sourceRow[FoldersTable.id].value
        val newId = insertAndGetId {
            it[folderName] = sourceRow[folderName]
            it[lastModified] = eventTimestamp
            it[note] = sourceRow[note]
            it[parentFolderID] = parentFolderId
            it[isFolderArchived] = sourceRow[isFolderArchived]
        }
        oldToNewIdMap[oldId] = newId.value
    }
    return oldToNewIdMap
}

fun LinksTable.copy(
    source: List<ResultRow>, eventTimestamp: Long, parentFolderId: Long?, newLinkType: LinkType? = null
): Map<Long, Long> {
    val oldToNewIdMap = mutableMapOf<Long, Long>()
    source.forEach { sourceRow ->
        val oldId = sourceRow[LinksTable.id].value
        val newId = insertAndGetId {
            it[lastModified] = eventTimestamp
            it[linkType] = newLinkType?.name ?: sourceRow[LinksTable.linkType]
            it[linkTitle] = sourceRow[LinksTable.linkTitle]
            it[url] = sourceRow[LinksTable.url]
            it[baseURL] = sourceRow[LinksTable.baseURL]
            it[imgURL] = sourceRow[LinksTable.imgURL]
            it[note] = sourceRow[LinksTable.note]
            it[idOfLinkedFolder] = parentFolderId
            it[userAgent] = sourceRow[LinksTable.userAgent]
            it[mediaType] = sourceRow[LinksTable.mediaType]
            it[markedAsImportant] = sourceRow[LinksTable.markedAsImportant]
        }
        oldToNewIdMap[oldId] = newId.value
    }
    return oldToNewIdMap
}

inline fun <T> tryAndCatchResult(init: () -> Result<T>): Result<T> {
    return try {
        init()
    } catch (e: Exception) {
        Result.Failure(e)
    }
}