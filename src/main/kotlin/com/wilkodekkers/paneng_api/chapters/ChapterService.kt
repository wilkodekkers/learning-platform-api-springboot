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
    fun findAll(bookId: Long): List<ChapterResponse> {
        if (!bookRepository.existsById(bookId)) {
            throw NotFoundException("Book with ID $bookId not found")
        }

        return chapterRepository
            .findAllByBookIdOrderByPositionAsc(bookId)
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun findById(bookId: Long, chapterId: Long): ChapterResponse {
        if (!bookRepository.existsById(bookId)) {
            throw NotFoundException("Book with ID $bookId not found")
        }

        val chapter = findChapter(bookId, chapterId)

        return chapter.toResponse()
    }

    @Transactional
    fun create(
        request: CreateChapterRequest,
        bookId: Long
    ): ChapterResponse {
        val book = bookRepository.findById(bookId)
            .orElseThrow {
                NotFoundException("Book with ID $bookId not found")
            }

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
        request: UpdateChapterRequest
    ): ChapterResponse {
        if (!bookRepository.existsById(bookId)) {
            throw NotFoundException("Book with ID $bookId not found")
        }

        val chapter = findChapter(bookId, chapterId)

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
    fun delete(bookId: Long, chapterId: Long) {
        if (!bookRepository.existsById(bookId)) {
            throw NotFoundException("Book with ID $bookId not found")
        }

        val chapter = findChapter(bookId, chapterId)

        chapterRepository.delete(chapter)
    }

    private fun findChapter(bookId: Long, chapterId: Long): Chapter {
        return chapterRepository.findByIdAndBookId(chapterId, bookId)
            ?: throw NotFoundException(
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
