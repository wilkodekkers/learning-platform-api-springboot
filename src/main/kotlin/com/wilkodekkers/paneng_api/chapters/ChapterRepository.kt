package com.wilkodekkers.paneng_api.chapters

import org.springframework.data.jpa.repository.JpaRepository

interface ChapterRepository : JpaRepository<Chapter, Long> {
    fun findAllByBookIdOrderByPositionAsc(bookId: Long): List<Chapter>

    fun findByIdAndBookId(chapterId: Long, bookId: Long): Chapter?

    fun existsByBookIdAndPosition(bookId: Long, position: Int): Boolean

    fun existsByBookIdAndPositionAndIdNot(
        bookId: Long,
        position: Int,
        chapterId: Long
    ): Boolean
}
