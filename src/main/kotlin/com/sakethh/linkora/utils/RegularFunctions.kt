package com.sakethh.linkora.utils

fun hostedOnRemote(): Boolean {
    return try {
        System.getenv(SysEnvKey.LINKORA_SERVER_ON_REMOTE.name).toBooleanStrict()
    } catch (e: Exception) {
        false
    }
}