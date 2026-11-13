
package com.wilkodekkers.paneng_api.books

import com.wilkodekkers.paneng_api.books.dto.CreateBookRequest
import com.wilkodekkers.paneng_api.books.dto.UpdateBookRequest
import com.wilkodekkers.paneng_api.common.exceptions.NotFoundException
import com.wilkodekkers.paneng_api.users.User
import com.wilkodekkers.paneng_api.users.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.Optional

class BookServiceTests {

    private val bookRepository = mock(BookRepository::class.java)
    private val userRepository = mock(UserRepository::class.java)

    private val bookService = BookService(
        bookRepository,
        userRepository
    )

    private val user = User(
        id = 1L,
        name = "Test User",
        email = "test@example.com",
        password = "hashed-password"
    )

    private val book1 = Book(
        id = 1L,
        title = "Thai Basics",
        author = "John Smith",
        user = user
    )

    private val book2 = Book(
        id = 2L,
        title = "Learning Kotlin",
        author = "Jane Doe",
        user = user
    )

    @BeforeEach
    fun setUp() {
        // Reset mutable test data before each test if necessary.
    }

    // FIND ALL

    @Test
    fun `findAll should return all books belonging to a user`() {
        `when`(bookRepository.findAllByUserId(1L))
            .thenReturn(listOf(book1, book2))

        val result = bookService.findAll(1L)

        assertEquals(2, result.size)
        assertEquals("Thai Basics", result[0].title)
        assertEquals("Learning Kotlin", result[1].title)

        verify(bookRepository).findAllByUserId(1L)
    }

    @Test
    fun `findAll should return empty list when user has no books`() {
        `when`(bookRepository.findAllByUserId(1L))
            .thenReturn(emptyList())

        val result = bookService.findAll(1L)

        assertTrue(result.isEmpty())
        verify(bookRepository).findAllByUserId(1L)
    }

    // FIND BY ID

    @Test
    fun `findById should return book when it exists`() {
        `when`(bookRepository.findByIdAndUserId(1L, 1L))
            .thenReturn(Optional.of(book1))

        val result = bookService.findById(1L, 1L)

        assertEquals(1L, result.id)
        assertEquals("Thai Basics", result.title)
        assertEquals("John Smith", result.author)

        verify(bookRepository).findByIdAndUserId(1L, 1L)
    }

    @Test
    fun `findById should throw NotFoundException when book does not exist`() {
        `when`(bookRepository.findByIdAndUserId(1L, 1L))
            .thenReturn(Optional.empty())

        assertThrows(NotFoundException::class.java) {
            bookService.findById(1L, 1L)
        }
    }

    // CREATE

    @Test
    fun `create should create book for user`() {
        val request = CreateBookRequest(
            title = "New Book",
            author = "New Author"
        )

        `when`(userRepository.getReferenceById(1L))
            .thenReturn(user)

        `when`(bookRepository.save(org.mockito.ArgumentMatchers.any(Book::class.java)))
            .thenAnswer { invocation ->
                invocation.getArgument<Book>(0)
            }

        val result = bookService.create(1L, request)

        assertEquals("New Book", result.title)
        assertEquals("New Author", result.author)

        verify(userRepository).getReferenceById(1L)
        verify(bookRepository).save(org.mockito.ArgumentMatchers.any(Book::class.java))
    }

    // UPDATE

    @Test
    fun `update should update book title and author`() {
        val request = UpdateBookRequest(
            title = "Updated Title",
            author = "Updated Author"
        )

        `when`(bookRepository.findByIdAndUserId(1L, 1L))
            .thenReturn(Optional.of(book1))

        val result = bookService.update(1L, 1L, request)

        assertEquals("Updated Title", result.title)
        assertEquals("Updated Author", result.author)

        verify(bookRepository).findByIdAndUserId(1L, 1L)
    }

    @Test
    fun `update should throw NotFoundException when book does not exist`() {
        val request = UpdateBookRequest(
            title = "Updated Title",
            author = "Updated Author"
        )

        `when`(bookRepository.findByIdAndUserId(1L, 1L))
            .thenReturn(Optional.empty())

        assertThrows(NotFoundException::class.java) {
            bookService.update(1L, 1L, request)
        }
    }

    // DELETE

    @Test
    fun `delete should delete book when it exists`() {
        `when`(bookRepository.findByIdAndUserId(1L, 1L))
            .thenReturn(Optional.of(book1))

        bookService.delete(1L, 1L)

        verify(bookRepository).findByIdAndUserId(1L, 1L)
        verify(bookRepository).delete(book1)
    }

    @Test
    fun `delete should throw NotFoundException when book does not exist`() {
        `when`(bookRepository.findByIdAndUserId(1L, 1L))
            .thenReturn(Optional.empty())

        assertThrows(NotFoundException::class.java) {
            bookService.delete(1L, 1L)
        }

        verify(bookRepository).findByIdAndUserId(1L, 1L)
    }
}
