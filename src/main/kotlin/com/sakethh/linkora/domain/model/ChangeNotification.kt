package com.sakethh.linkora.domain.model

data class ChangeNotification(
    val operation: String,
    val payload: Any
)
