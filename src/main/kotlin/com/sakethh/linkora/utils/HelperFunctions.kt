package com.sakethh.linkora.utils

import com.sakethh.linkora.domain.LWWConflictException
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.transactions.transaction

fun useSysEnvValues(): Boolean {
    return try {
        System.getenv(SysEnvKey.LINKORA_SERVER_USE_ENV_VAL.name).toBooleanStrict()
    } catch (e: Exception) {
        false
    }
}

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