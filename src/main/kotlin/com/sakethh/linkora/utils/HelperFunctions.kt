package com.sakethh.linkora.utils

import com.sakethh.linkora.domain.LWWConflictException
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.tables.FoldersTable
import com.sakethh.linkora.domain.tables.LinksTable
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.jdbc.batchInsert
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

fun FoldersTable.copy(source: List<ResultRow>, eventTimestamp: Long, parentFolderId: Long?): List<ResultRow> {
    return batchInsert(source) {
        this[folderName] = it[folderName]
        this[lastModified] = eventTimestamp
        this[note] = it[note]
        this[parentFolderID] = parentFolderId
        this[isFolderArchived] = it[isFolderArchived]
    }.toList()
}

fun LinksTable.copy(source: List<ResultRow>, eventTimestamp: Long, parentFolderId: Long?): List<ResultRow> {
    return batchInsert(source) {
        set(lastModified, eventTimestamp)
        set(linkType, it[linkType])
        set(linkTitle, it[linkTitle])
        set(url, it[url])
        set(baseURL, it[baseURL])
        set(imgURL, it[imgURL])
        set(note, it[note])
        set(idOfLinkedFolder, parentFolderId)
        set(userAgent, it[userAgent])
        set(mediaType, it[mediaType])
        set(markedAsImportant, it[markedAsImportant])
    }
}

inline fun <T> tryAndCatchResult(init: () -> Result<T>): Result<T> {
    return try {
        init()
    } catch (e: Exception) {
        Result.Failure(e)
    }
}