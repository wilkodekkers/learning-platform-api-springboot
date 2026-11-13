
package com.wilkodekkers.paneng_api.chapters

import com.wilkodekkers.paneng_api.books.Book
import com.wilkodekkers.paneng_api.books.BookRepository
import com.wilkodekkers.paneng_api.chapters.dto.CreateChapterRequest
import com.wilkodekkers.paneng_api.chapters.dto.UpdateChapterRequest
import com.wilkodekkers.paneng_api.common.exceptions.ConflictException
import com.wilkodekkers.paneng_api.common.exceptions.NotFoundException
import com.wilkodekkers.paneng_api.users.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.Instant
import java.util.Optional

class ChapterServiceTests {

    private val chapterRepository =
        mock(ChapterRepository::class.java)

    private val bookRepository =
        mock(BookRepository::class.java)

    private val chapterService = ChapterService(
        chapterRepository,
        bookRepository
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

    private fun createChapter(
        id: Long = 1L,
        title: String = "Introduction",
        position: Int = 1
    ) = Chapter(
        id = id,
        book = book,
        title = title,
        position = position
    )

    // FIND ALL

    @Test
    fun `findAll should return chapters ordered by position`() {
        val chapter1 = createChapter(
            id = 1L,
            title = "Introduction",
            position = 1
        )
        val chapter2 = createChapter(
            id = 2L,
            title = "Greetings",
            position = 2
        )

        `when`(bookRepository.findByIdAndUserId(1L, 1L))
            .thenReturn(Optional.of(book))

        `when`(
            chapterRepository.findAllByBookIdAndBookUserIdOrderByPositionAsc(
                1L,
                1L
            )
        ).thenReturn(listOf(chapter1, chapter2))

        val result = chapterService.findAll(1L, 1L)

        assertEquals(2, result.size)
        assertEquals("Introduction", result[0].title)
        assertEquals(1, result[0].position)
        assertEquals("Greetings", result[1].title)
        assertEquals(2, result[1].position)

        verify(bookRepository).findByIdAndUserId(1L, 1L)
        verify(chapterRepository)
            .findAllByBookIdAndBookUserIdOrderByPositionAsc(1L, 1L)
    }

    @Test
    fun `findAll should return empty list when book has no chapters`() {
        `when`(bookRepository.findByIdAndUserId(1L, 1L))
            .thenReturn(Optional.of(book))

        `when`(
            chapterRepository.findAllByBookIdAndBookUserIdOrderByPositionAsc(
                1L,
                1L
            )
        ).thenReturn(emptyList())

        val result = chapterService.findAll(1L, 1L)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `findAll should throw NotFoundException when book does not belong to user`() {
        `when`(bookRepository.findByIdAndUserId(1L, 2L))
            .thenReturn(Optional.empty())

        assertThrows(NotFoundException::class.java) {
            chapterService.findAll(1L, 2L)
        }
    }

    // FIND BY ID

    @Test
    fun `findById should return chapter when it exists`() {
        val chapter = createChapter()

        `when`(
            chapterRepository.findByIdAndBookIdAndBookUserId(
                1L,
                1L,
                1L
            )
        ).thenReturn(chapter)

        val result = chapterService.findById(1L, 1L, 1L)

        assertEquals(1L, result.id)
        assertEquals(1L, result.bookId)
        assertEquals("Introduction", result.title)
        assertEquals(1, result.position)

        verify(chapterRepository)
            .findByIdAndBookIdAndBookUserId(1L, 1L, 1L)
    }

    @Test
    fun `findById should throw NotFoundException when chapter does not exist`() {
        `when`(
            chapterRepository.findByIdAndBookIdAndBookUserId(
                1L,
                1L,
                1L
            )
        ).thenReturn(null)

        assertThrows(NotFoundException::class.java) {
            chapterService.findById(1L, 1L, 1L)
        }
    }

    // CREATE

    @Test
    fun `create should create chapter for book`() {
        val request = CreateChapterRequest(
            title = "New Chapter",
            position = 3
        )

        `when`(bookRepository.findByIdAndUserId(1L, 1L))
            .thenReturn(Optional.of(book))

        `when`(chapterRepository.existsByBookIdAndPosition(1L, 3))
            .thenReturn(false)

        `when`(chapterRepository.save(any(Chapter::class.java)))
            .thenAnswer { invocation ->
                invocation.getArgument<Chapter>(0)
            }

        val result = chapterService.create(request, 1L, 1L)

        assertEquals("New Chapter", result.title)
        assertEquals(3, result.position)
        assertEquals(1L, result.bookId)

        verify(bookRepository).findByIdAndUserId(1L, 1L)
        verify(chapterRepository).existsByBookIdAndPosition(1L, 3)
        verify(chapterRepository).save(any(Chapter::class.java))
    }

    @Test
    fun `create should throw ConflictException when position is already occupied`() {
        val request = CreateChapterRequest(
            title = "New Chapter",
            position = 1
        )

        `when`(bookRepository.findByIdAndUserId(1L, 1L))
            .thenReturn(Optional.of(book))

        `when`(chapterRepository.existsByBookIdAndPosition(1L, 1))
            .thenReturn(true)

        assertThrows(ConflictException::class.java) {
            chapterService.create(request, 1L, 1L)
        }
    }

    @Test
    fun `create should throw NotFoundException when book does not exist`() {
        val request = CreateChapterRequest(
            title = "New Chapter",
            position = 1
        )

        `when`(bookRepository.findByIdAndUserId(1L, 1L))
            .thenReturn(Optional.empty())

        assertThrows(NotFoundException::class.java) {
            chapterService.create(request, 1L, 1L)
        }
    }

    // UPDATE

    @Test
    fun `update should update chapter title and position`() {
        val oldTimestamp = Instant.parse("2025-01-01T00:00:00Z")
        val chapter = Chapter(
            id = 1L,
            book = book,
            title = "Old Title",
            position = 1,
            updatedAt = oldTimestamp
        )

        val request = UpdateChapterRequest(
            title = "Updated Title",
            position = 2
        )

        `when`(
            chapterRepository.findByIdAndBookIdAndBookUserId(
                1L,
                1L,
                1L
            )
        ).thenReturn(chapter)

        `when`(
            chapterRepository.existsByBookIdAndPositionAndIdNot(
                1L,
                2,
                1L
            )
        ).thenReturn(false)

        val result = chapterService.update(
            1L,
            1L,
            request,
            1L
        )

        assertEquals("Updated Title", result.title)
        assertEquals(2, result.position)
        assertTrue(result.updatedAt.isAfter(oldTimestamp))
    }

    @Test
    fun `update should throw ConflictException when position is occupied by another chapter`() {
        val chapter = createChapter()

        val request = UpdateChapterRequest(
            title = "Updated Title",
            position = 2
        )

        `when`(
            chapterRepository.findByIdAndBookIdAndBookUserId(
                1L,
                1L,
                1L
            )
        ).thenReturn(chapter)

        `when`(
            chapterRepository.existsByBookIdAndPositionAndIdNot(
                1L,
                2,
                1L
            )
        ).thenReturn(true)

        assertThrows(ConflictException::class.java) {
            chapterService.update(1L, 1L, request, 1L)
        }
    }

    @Test
    fun `update should throw NotFoundException when chapter does not exist`() {
        val request = UpdateChapterRequest(
            title = "Updated Title",
            position = 2
        )

        `when`(
            chapterRepository.findByIdAndBookIdAndBookUserId(
                1L,
                1L,
                1L
            )
        ).thenReturn(null)

        assertThrows(NotFoundException::class.java) {
            chapterService.update(1L, 1L, request, 1L)
        }
    }

    // DELETE

    @Test
    fun `delete should delete chapter when it exists`() {
        val chapter = createChapter()

        `when`(
            chapterRepository.findByIdAndBookIdAndBookUserId(
                1L,
                1L,
                1L
            )
        ).thenReturn(chapter)

        chapterService.delete(1L, 1L, 1L)

        verify(chapterRepository)
            .findByIdAndBookIdAndBookUserId(1L, 1L, 1L)
        verify(chapterRepository).delete(chapter)
    }

    @Test
    fun `delete should throw NotFoundException when chapter does not exist`() {
        `when`(
            chapterRepository.findByIdAndBookIdAndBookUserId(
                1L,
                1L,
                1L
            )
        ).thenReturn(null)

        assertThrows(NotFoundException::class.java) {
            chapterService.delete(1L, 1L, 1L)
        }
    }
}
