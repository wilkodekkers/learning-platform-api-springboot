package com.wilkodekkers.paneng_api.chapters.dto

import java.time.Instant

data class ChapterResponse(
    val id: Long,
    val bookId: Long,
    val title: String,
    val position: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
)
