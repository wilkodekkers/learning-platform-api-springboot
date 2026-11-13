package com.wilkodekkers.paneng_api.vocabulary

import org.springframework.data.jpa.repository.JpaRepository

interface VocabularyItemRepository : JpaRepository<VocabularyItem, Long> {
    fun findAllByChapterIdAndChapterBookUserIdOrderByIdAsc(
        chapterId: Long,
        userId: Long
    ): List<VocabularyItem>

    fun findByIdAndChapterIdAndChapterBookIdAndChapterBookUserId(
        id: Long,
        chapterId: Long,
        bookId: Long,
        userId: Long
    ): VocabularyItem?
}
