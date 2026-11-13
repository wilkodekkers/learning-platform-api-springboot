
package com.wilkodekkers.paneng_api.vocabulary

import com.wilkodekkers.paneng_api.books.Book
import com.wilkodekkers.paneng_api.chapters.Chapter
import com.wilkodekkers.paneng_api.chapters.ChapterRepository
import com.wilkodekkers.paneng_api.common.exceptions.NotFoundException
import com.wilkodekkers.paneng_api.users.User
import com.wilkodekkers.paneng_api.vocabulary.dto.CreateVocabularyItemRequest
import com.wilkodekkers.paneng_api.vocabulary.dto.UpdateVocabularyItemRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.Instant

class VocabularyItemServiceTests {

    private val vocabularyItemRepository =
        mock(VocabularyItemRepository::class.java)

    private val chapterRepository =
        mock(ChapterRepository::class.java)

    private val vocabularyItemService = VocabularyItemService(
        vocabularyItemRepository,
        chapterRepository
    )

    private val user = User(
        id = 1L,
        name = "Test User",
        email = "test@example.com",
        password = "hashed-password"
    )

    private val book = Book(
        id = 1L,
        title = "Thai Basics",
        author = "John Smith",
        user = user
    )

    private val chapter = Chapter(
        id = 1L,
        book = book,
        title = "Introduction",
        position = 1
    )

    private fun createVocabularyItem(
        id: Long = 1L,
        term: String = "กิน",
        definition: String = "To eat"
    ) = VocabularyItem(
        id = id,
        chapter = chapter,
        term = term,
        definition = definition
    )

    // FIND ALL

    @Test
    fun `findAll should return vocabulary items belonging to chapter`() {
        val item1 = createVocabularyItem(
            id = 1L,
            term = "กิน",
            definition = "To eat"
        )

        val item2 = createVocabularyItem(
            id = 2L,
            term = "ดื่ม",
            definition = "To drink"
        )

        `when`(
            chapterRepository.findByIdAndBookIdAndBookUserId(
                1L,
                1L,
                1L
            )
        ).thenReturn(chapter)

        `when`(
            vocabularyItemRepository
                .findAllByChapterIdAndChapterBookUserIdOrderByIdAsc(
                    1L,
                    1L
                )
        ).thenReturn(listOf(item1, item2))

        val result = vocabularyItemService.findAll(1L, 1L, 1L)

        assertEquals(2, result.size)
        assertEquals("กิน", result[0].term)
        assertEquals("To eat", result[0].definition)
        assertEquals("ดื่ม", result[1].term)
        assertEquals("To drink", result[1].definition)

        verify(chapterRepository)
            .findByIdAndBookIdAndBookUserId(1L, 1L, 1L)

        verify(vocabularyItemRepository)
            .findAllByChapterIdAndChapterBookUserIdOrderByIdAsc(1L, 1L)
    }

    @Test
    fun `findAll should return empty list when chapter has no vocabulary items`() {
        `when`(
            chapterRepository.findByIdAndBookIdAndBookUserId(
                1L,
                1L,
                1L
            )
        ).thenReturn(chapter)

        `when`(
            vocabularyItemRepository
                .findAllByChapterIdAndChapterBookUserIdOrderByIdAsc(
                    1L,
                    1L
                )
        ).thenReturn(emptyList())

        val result = vocabularyItemService.findAll(1L, 1L, 1L)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `findAll should throw NotFoundException when chapter does not belong to user`() {
        `when`(
            chapterRepository.findByIdAndBookIdAndBookUserId(
                1L,
                1L,
                2L
            )
        ).thenReturn(null)

        assertThrows(NotFoundException::class.java) {
            vocabularyItemService.findAll(1L, 1L, 2L)
        }
    }

    // FIND BY ID

    @Test
    fun `findById should return vocabulary item when it exists`() {
        val item = createVocabularyItem()

        `when`(
            vocabularyItemRepository
                .findByIdAndChapterIdAndChapterBookIdAndChapterBookUserId(
                    1L,
                    1L,
                    1L,
                    1L
                )
        ).thenReturn(item)

        val result = vocabularyItemService.findById(
            1L,
            1L,
            1L,
            1L
        )

        assertEquals(1L, result.id)
        assertEquals(1L, result.chapterId)
        assertEquals("กิน", result.term)
        assertEquals("To eat", result.definition)

        verify(vocabularyItemRepository)
            .findByIdAndChapterIdAndChapterBookIdAndChapterBookUserId(
                1L,
                1L,
                1L,
                1L
            )
    }

    @Test
    fun `findById should throw NotFoundException when vocabulary item does not exist`() {
        `when`(
            vocabularyItemRepository
                .findByIdAndChapterIdAndChapterBookIdAndChapterBookUserId(
                    1L,
                    1L,
                    1L,
                    1L
                )
        ).thenReturn(null)

        assertThrows(NotFoundException::class.java) {
            vocabularyItemService.findById(1L, 1L, 1L, 1L)
        }
    }

    // CREATE

    @Test
    fun `create should create vocabulary item for chapter`() {
        val request = CreateVocabularyItemRequest(
            term = "กิน",
            definition = "To eat"
        )

        `when`(
            chapterRepository.findByIdAndBookIdAndBookUserId(
                1L,
                1L,
                1L
            )
        ).thenReturn(chapter)

        `when`(
            vocabularyItemRepository.save(any(VocabularyItem::class.java))
        ).thenAnswer { invocation ->
            invocation.getArgument<VocabularyItem>(0)
        }

        val result = vocabularyItemService.create(
            1L,
            1L,
            1L,
            request
        )

        assertEquals("กิน", result.term)
        assertEquals("To eat", result.definition)
        assertEquals(1L, result.chapterId)

        verify(chapterRepository)
            .findByIdAndBookIdAndBookUserId(1L, 1L, 1L)

        verify(vocabularyItemRepository)
            .save(any(VocabularyItem::class.java))
    }

    @Test
    fun `create should throw NotFoundException when chapter does not belong to user`() {
        val request = CreateVocabularyItemRequest(
            term = "กิน",
            definition = "To eat"
        )

        `when`(
            chapterRepository.findByIdAndBookIdAndBookUserId(
                1L,
                1L,
                2L
            )
        ).thenReturn(null)

        assertThrows(NotFoundException::class.java) {
            vocabularyItemService.create(1L, 1L, 2L, request)
        }
    }

    // UPDATE

    @Test
    fun `update should update vocabulary item term and definition`() {
        val oldTimestamp = Instant.parse("2025-01-01T00:00:00Z")

        val item = VocabularyItem(
            id = 1L,
            chapter = chapter,
            term = "กิน",
            definition = "Old definition",
            updatedAt = oldTimestamp
        )

        val request = UpdateVocabularyItemRequest(
            term = "รับประทาน",
            definition = "To eat (formal)"
        )

        `when`(
            vocabularyItemRepository
                .findByIdAndChapterIdAndChapterBookIdAndChapterBookUserId(
                    1L,
                    1L,
                    1L,
                    1L
                )
        ).thenReturn(item)

        val result = vocabularyItemService.update(
            1L,
            1L,
            1L,
            1L,
            request
        )

        assertEquals("รับประทาน", result.term)
        assertEquals("To eat (formal)", result.definition)
        assertTrue(result.updatedAt.isAfter(oldTimestamp))
    }

    @Test
    fun `update should throw NotFoundException when vocabulary item does not exist`() {
        val request = UpdateVocabularyItemRequest(
            term = "รับประทาน",
            definition = "To eat (formal)"
        )

        `when`(
            vocabularyItemRepository
                .findByIdAndChapterIdAndChapterBookIdAndChapterBookUserId(
                    1L,
                    1L,
                    1L,
                    1L
                )
        ).thenReturn(null)

        assertThrows(NotFoundException::class.java) {
            vocabularyItemService.update(
                1L,
                1L,
                1L,
                1L,
                request
            )
        }
    }

    // DELETE

    @Test
    fun `delete should delete vocabulary item when it exists`() {
        val item = createVocabularyItem()

        `when`(
            vocabularyItemRepository
                .findByIdAndChapterIdAndChapterBookIdAndChapterBookUserId(
                    1L,
                    1L,
                    1L,
                    1L
                )
        ).thenReturn(item)

        vocabularyItemService.delete(1L, 1L, 1L, 1L)

        verify(vocabularyItemRepository)
            .findByIdAndChapterIdAndChapterBookIdAndChapterBookUserId(
                1L,
                1L,
                1L,
                1L
            )

        verify(vocabularyItemRepository).delete(item)
    }

    @Test
    fun `delete should throw NotFoundException when vocabulary item does not exist`() {
        `when`(
            vocabularyItemRepository
                .findByIdAndChapterIdAndChapterBookIdAndChapterBookUserId(
                    1L,
                    1L,
                    1L,
                    1L
                )
        ).thenReturn(null)

        assertThrows(NotFoundException::class.java) {
            vocabularyItemService.delete(1L, 1L, 1L, 1L)
        }
    }
}
