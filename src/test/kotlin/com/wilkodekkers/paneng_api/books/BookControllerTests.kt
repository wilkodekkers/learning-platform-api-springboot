
package com.wilkodekkers.paneng_api.books

import com.wilkodekkers.paneng_api.books.dto.BookResponse
import com.wilkodekkers.paneng_api.books.dto.CreateBookRequest
import com.wilkodekkers.paneng_api.books.dto.UpdateBookRequest
import com.wilkodekkers.paneng_api.common.exceptions.NotFoundException
import com.wilkodekkers.paneng_api.security.CurrentUser
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.time.Instant

@WebMvcTest(
    controllers = [BookController::class],
    excludeAutoConfiguration = [
        SecurityAutoConfiguration::class,
        OAuth2ResourceServerAutoConfiguration::class
    ]
)
class BookControllerTests(
    @Autowired private val mockMvc: MockMvc
) {

    @MockitoBean
    private lateinit var bookService: BookService

    @MockitoBean
    private lateinit var currentUser: CurrentUser

    private val book1 = BookResponse(
        id = 1L,
        title = "Thai Basics",
        author = "John Smith",
        createdAt = Instant.parse("2025-01-01T10:00:00Z"),
        updatedAt = Instant.parse("2025-01-01T10:00:00Z")
    )

    private val book2 = BookResponse(
        id = 2L,
        title = "Learning Kotlin",
        author = "Jane Doe",
        createdAt = Instant.parse("2025-01-02T10:00:00Z"),
        updatedAt = Instant.parse("2025-01-02T10:00:00Z")
    )

    // GET /api/books

    @Test
    fun `GET books should return all books belonging to current user`() {
        `when`(currentUser.getId()).thenReturn(1L)
        `when`(bookService.findAll(1L))
            .thenReturn(listOf(book1, book2))

        mockMvc.get("/api/books")
            .andExpect {
                status { isOk() }
                jsonPath("$.length()") { value(2) }
                jsonPath("$[0].id") { value(1) }
                jsonPath("$[0].title") { value("Thai Basics") }
                jsonPath("$[1].id") { value(2) }
                jsonPath("$[1].title") { value("Learning Kotlin") }
            }

        verify(bookService).findAll(1L)
    }

    @Test
    fun `GET books should return empty list when user has no books`() {
        `when`(currentUser.getId()).thenReturn(1L)
        `when`(bookService.findAll(1L))
            .thenReturn(emptyList())

        mockMvc.get("/api/books")
            .andExpect {
                status { isOk() }
                content { json("[]") }
            }

        verify(bookService).findAll(1L)
    }

    // GET /api/books/{id}

    @Test
    fun `GET book by id should return book when it exists`() {
        `when`(currentUser.getId()).thenReturn(1L)
        `when`(bookService.findById(1L, 1L))
            .thenReturn(book1)

        mockMvc.get("/api/books/1")
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value(1) }
                jsonPath("$.title") { value("Thai Basics") }
                jsonPath("$.author") { value("John Smith") }
            }

        verify(bookService).findById(1L, 1L)
    }

    @Test
    fun `GET book by id should return not found when book does not exist`() {
        `when`(currentUser.getId()).thenReturn(1L)
        `when`(bookService.findById(999L, 1L))
            .thenThrow(NotFoundException("Book with ID 999 not found"))

        mockMvc.get("/api/books/999")
            .andExpect {
                status { isNotFound() }
            }

        verify(bookService).findById(999L, 1L)
    }

    // POST /api/books

    @Test
    fun `POST book should create and return book`() {
        val request = CreateBookRequest(
            title = "Thai Basics",
            author = "John Smith"
        )

        `when`(currentUser.getId()).thenReturn(1L)
        `when`(bookService.create(1L, request))
            .thenReturn(book1)

        mockMvc.post("/api/books") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "title": "Thai Basics",
                    "author": "John Smith"
                }
            """.trimIndent()
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.title") { value("Thai Basics") }
            jsonPath("$.author") { value("John Smith") }
        }

        verify(bookService).create(1L, request)
    }

    @Test
    fun `POST book should return bad request when title is blank`() {
        mockMvc.post("/api/books") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "title": "",
                    "author": "John Smith"
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `POST book should return bad request when author is blank`() {
        mockMvc.post("/api/books") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "title": "Thai Basics",
                    "author": ""
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
        }
    }

    // PUT /api/books/{id}

    @Test
    fun `PUT book should update and return book`() {
        val request = UpdateBookRequest(
            title = "Updated Thai Basics",
            author = "Jane Smith"
        )

        val updatedBook = book1.copy(
            title = "Updated Thai Basics",
            author = "Jane Smith"
        )

        `when`(currentUser.getId()).thenReturn(1L)
        `when`(bookService.update(1L, 1L, request))
            .thenReturn(updatedBook)

        mockMvc.put("/api/books/1") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "title": "Updated Thai Basics",
                    "author": "Jane Smith"
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.title") { value("Updated Thai Basics") }
            jsonPath("$.author") { value("Jane Smith") }
        }

        verify(bookService).update(1L, 1L, request)
    }

    @Test
    fun `PUT book should return not found when book does not exist`() {
        val request = UpdateBookRequest(
            title = "Updated Thai Basics",
            author = "Jane Smith"
        )

        `when`(currentUser.getId()).thenReturn(1L)
        `when`(bookService.update(999L, 1L, request))
            .thenThrow(NotFoundException("Book with ID 999 not found"))

        mockMvc.put("/api/books/999") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "title": "Updated Thai Basics",
                    "author": "Jane Smith"
                }
            """.trimIndent()
        }.andExpect {
            status { isNotFound() }
        }

        verify(bookService).update(999L, 1L, request)
    }

    // DELETE /api/books/{id}

    @Test
    fun `DELETE book should return no content when book is deleted`() {
        `when`(currentUser.getId()).thenReturn(1L)
        doNothing().`when`(bookService).delete(1L, 1L)

        mockMvc.delete("/api/books/1")
            .andExpect {
                status { isNoContent() }
                content { string("") }
            }

        verify(bookService).delete(1L, 1L)
    }

    @Test
    fun `DELETE book should return not found when book does not exist`() {
        `when`(currentUser.getId()).thenReturn(1L)
        org.mockito.Mockito.doThrow(
            NotFoundException("Book with ID 999 not found")
        ).`when`(bookService).delete(999L, 1L)

        mockMvc.delete("/api/books/999")
            .andExpect {
                status { isNotFound() }
            }

        verify(bookService).delete(999L, 1L)
    }
}
