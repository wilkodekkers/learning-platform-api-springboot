package com.wilkodekkers.paneng_api.chapters

import org.springframework.data.jpa.repository.JpaRepository

interface ChapterRepository : JpaRepository<Chapter, Long> {

    fun findAllByBookIdAndBookUserIdOrderByPositionAsc(
        bookId: Long,
        userId: Long
    ): List<Chapter>

    fun findByIdAndBookIdAndBookUserId(
        chapterId: Long,
        bookId: Long,
        userId: Long
    ): Chapter?

    fun existsByBookIdAndPosition(
        bookId: Long,
        position: Int
    ): Boolean

    fun existsByBookIdAndPositionAndIdNot(
        bookId: Long,
        position: Int,
        chapterId: Long
    ): Boolean
}
