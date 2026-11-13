package com.wilkodekkers.paneng_api.vocabulary.dto

import java.time.Instant

data class VocabularyItemResponse(
    val id: Long,
    val chapterId: Long,
    val term: String,
    val definition: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
