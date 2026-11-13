package com.wilkodekkers.paneng_api.books.dto

import java.time.Instant

data class BookResponse(
    val id: Long,
    val title: String,
    val author: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
