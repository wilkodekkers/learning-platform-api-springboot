package com.wilkodekkers.paneng_api.vocabulary

import com.wilkodekkers.paneng_api.chapters.ChapterRepository
import com.wilkodekkers.paneng_api.common.exceptions.NotFoundException
import com.wilkodekkers.paneng_api.vocabulary.dto.CreateVocabularyItemRequest
import com.wilkodekkers.paneng_api.vocabulary.dto.UpdateVocabularyItemRequest
import com.wilkodekkers.paneng_api.vocabulary.dto.VocabularyItemResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class VocabularyItemService(
    private val vocabularyItemRepository: VocabularyItemRepository,
    private val chapterRepository: ChapterRepository,
) {

    @Transactional(readOnly = true)
    fun findAll(
        bookId: Long,
        chapterId: Long,
        userId: Long
    ): List<VocabularyItemResponse> {
        findChapterByIdAndUserId(bookId, chapterId, userId)

        return vocabularyItemRepository
            .findAllByChapterIdAndChapterBookUserIdOrderByIdAsc(chapterId, userId)
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun findById(
        bookId: Long,
        chapterId: Long,
        vocabularyItemId: Long,
        userId: Long
    ): VocabularyItemResponse {
        return findVocabularyItem(
            bookId,
            chapterId,
            vocabularyItemId,
            userId
        ).toResponse()
    }

    @Transactional
    fun create(
        bookId: Long,
        chapterId: Long,
        userId: Long,
        request: CreateVocabularyItemRequest
    ): VocabularyItemResponse {
        val chapter = findChapterByIdAndUserId(bookId, chapterId, userId)

        val vocabularyItem = VocabularyItem(
            chapter = chapter,
            term = request.term,
            definition = request.definition,
        )

        return vocabularyItemRepository.save(vocabularyItem).toResponse()
    }

    @Transactional
    fun update(
        bookId: Long,
        chapterId: Long,
        vocabularyItemId: Long,
        userId: Long,
        request: UpdateVocabularyItemRequest
    ): VocabularyItemResponse {
        val vocabularyItem = findVocabularyItem(
            bookId,
            chapterId,
            vocabularyItemId,
            userId
        )

        vocabularyItem.term = request.term
        vocabularyItem.definition = request.definition
        vocabularyItem.updatedAt = Instant.now()

        return vocabularyItem.toResponse()
    }

    @Transactional
    fun delete(
        bookId: Long,
        chapterId: Long,
        vocabularyItemId: Long,
        userId: Long
    ) {
        val vocabularyItem = findVocabularyItem(
            bookId,
            chapterId,
            vocabularyItemId,
            userId
        )

        vocabularyItemRepository.delete(vocabularyItem)
    }

    private fun findChapterByIdAndUserId(
        bookId: Long,
        chapterId: Long,
        userId: Long
    ) = chapterRepository.findByIdAndBookIdAndBookUserId(
        chapterId,
        bookId,
        userId
    ) ?: throw NotFoundException(
        "Chapter with ID $chapterId not found in book with ID $bookId"
    )

    private fun findVocabularyItem(
        bookId: Long,
        chapterId: Long,
        vocabularyItemId: Long,
        userId: Long
    ): VocabularyItem {
        return vocabularyItemRepository
            .findByIdAndChapterIdAndChapterBookIdAndChapterBookUserId(
                vocabularyItemId,
                chapterId,
                bookId,
                userId
            ) ?: throw NotFoundException(
            "Vocabulary item with ID $vocabularyItemId not found"
        )
    }

    private fun VocabularyItem.toResponse() = VocabularyItemResponse(
        id = id,
        chapterId = chapter.id,
        term = term,
        definition = definition,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}