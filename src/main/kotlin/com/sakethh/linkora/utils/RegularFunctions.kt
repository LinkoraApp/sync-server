package com.sakethh.linkora.utils

fun useSysEnvValues(): Boolean {
    return try {
        System.getenv(SysEnvKey.LINKORA_SERVER_USE_ENV_VAL.name).toBooleanStrict()
    } catch (e: Exception) {
        false
    }
}