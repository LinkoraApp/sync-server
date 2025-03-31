package com.sakethh.linkora.utils

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlin.test.assertContains

fun String.assertContains(string: String) {
    assertContains(charSequence = this, other = string)
}

fun ApplicationTestBuilder.httpClient(): HttpClient {
    return client.use {
        it.config {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    encodeDefaults = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }
}