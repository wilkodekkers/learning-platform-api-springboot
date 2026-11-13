package com.wilkodekkers.paneng_api.chapters

import com.wilkodekkers.paneng_api.books.BookRepository
import com.wilkodekkers.paneng_api.chapters.dto.ChapterResponse
import com.wilkodekkers.paneng_api.chapters.dto.CreateChapterRequest
import com.wilkodekkers.paneng_api.chapters.dto.UpdateChapterRequest
import com.wilkodekkers.paneng_api.common.exceptions.ConflictException
import com.wilkodekkers.paneng_api.common.exceptions.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ChapterService(
    private val chapterRepository: ChapterRepository,
    private val bookRepository: BookRepository,
) {

    @Transactional(readOnly = true)
    fun findAll(bookId: Long, userId: Long): List<ChapterResponse> {
        findBookByIdAndUserId(bookId, userId)

        return chapterRepository
            .findAllByBookIdAndBookUserIdOrderByPositionAsc(bookId, userId)
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun findById(
        bookId: Long,
        chapterId: Long,
        userId: Long
    ): ChapterResponse {
        return findChapter(bookId, chapterId, userId).toResponse()
    }

    @Transactional
    fun create(
        request: CreateChapterRequest,
        bookId: Long,
        userId: Long
    ): ChapterResponse {
        val book = findBookByIdAndUserId(bookId, userId)

        if (chapterRepository.existsByBookIdAndPosition(bookId, request.position)) {
            throw ConflictException(
                "Chapter position ${request.position} is already occupied"
            )
        }

        val chapter = Chapter(
            title = request.title,
            book = book,
            position = request.position,
        )

        return chapterRepository.save(chapter).toResponse()
    }

    @Transactional
    fun update(
        bookId: Long,
        chapterId: Long,
        request: UpdateChapterRequest,
        userId: Long
    ): ChapterResponse {
        val chapter = findChapter(bookId, chapterId, userId)

        if (chapterRepository.existsByBookIdAndPositionAndIdNot(
                bookId,
                request.position,
                chapterId
            )
        ) {
            throw ConflictException(
                "Chapter position ${request.position} is already occupied"
            )
        }

        chapter.title = request.title
        chapter.position = request.position
        chapter.updatedAt = Instant.now()

        return chapter.toResponse()
    }

    @Transactional
    fun delete(bookId: Long, chapterId: Long, userId: Long) {
        val chapter = findChapter(bookId, chapterId, userId)
        chapterRepository.delete(chapter)
    }

    private fun findBookByIdAndUserId(bookId: Long, userId: Long) =
        bookRepository.findByIdAndUserId(bookId, userId)
            .orElseThrow {
                NotFoundException("Book with ID $bookId not found")
            }

    private fun findChapter(
        bookId: Long,
        chapterId: Long,
        userId: Long
    ): Chapter {
        return chapterRepository.findByIdAndBookIdAndBookUserId(
            chapterId,
            bookId,
            userId
        ) ?: throw NotFoundException(
            "Chapter with ID $chapterId not found in book with ID $bookId"
        )
    }

    private fun Chapter.toResponse() = ChapterResponse(
        id = id,
        bookId = book.id,
        title = title,
        position = position,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
