package com.wilkodekkers.paneng_api.chapters

import com.wilkodekkers.paneng_api.chapters.dto.ChapterResponse
import com.wilkodekkers.paneng_api.chapters.dto.CreateChapterRequest
import com.wilkodekkers.paneng_api.chapters.dto.UpdateChapterRequest
import com.wilkodekkers.paneng_api.common.exceptions.NotFoundException
import com.wilkodekkers.paneng_api.security.CurrentUser
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doThrow
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
    controllers = [ChapterController::class],
    excludeAutoConfiguration = [
        SecurityAutoConfiguration::class,
        OAuth2ResourceServerAutoConfiguration::class
    ]
)
class ChapterControllerTests(
    @Autowired private val mockMvc: MockMvc
) {

    @MockitoBean
    private lateinit var chapterService: ChapterService

    @MockitoBean
    private lateinit var currentUser: CurrentUser

    private val chapter1 = ChapterResponse(
        id = 1L,
        bookId = 1L,
        title = "Introduction",
        position = 1,
        createdAt = Instant.parse("2025-01-01T10:00:00Z"),
        updatedAt = Instant.parse("2025-01-01T10:00:00Z")
    )

    private val chapter2 = ChapterResponse(
        id = 2L,
        bookId = 1L,
        title = "Basic Vocabulary",
        position = 2,
        createdAt = Instant.parse("2025-01-02T10:00:00Z"),
        updatedAt = Instant.parse("2025-01-02T10:00:00Z")
    )

    // GET /api/books/{bookId}/chapters

    @Test
    fun `GET chapters should return all chapters belonging to book`() {
        `when`(currentUser.getId()).thenReturn(1L)
        `when`(chapterService.findAll(1L, 1L))
            .thenReturn(listOf(chapter1, chapter2))

        mockMvc.get("/api/books/1/chapters")
            .andExpect {
                status { isOk() }
                jsonPath("$.length()") { value(2) }
                jsonPath("$[0].id") { value(1) }
                jsonPath("$[0].bookId") { value(1) }
                jsonPath("$[0].title") { value("Introduction") }
                jsonPath("$[0].position") { value(1) }
                jsonPath("$[1].id") { value(2) }
                jsonPath("$[1].title") { value("Basic Vocabulary") }
                jsonPath("$[1].position") { value(2) }
            }

        verify(chapterService).findAll(1L, 1L)
    }

    @Test
    fun `GET chapters should return empty list when book has no chapters`() {
        `when`(currentUser.getId()).thenReturn(1L)
        `when`(chapterService.findAll(1L, 1L))
            .thenReturn(emptyList())

        mockMvc.get("/api/books/1/chapters")
            .andExpect {
                status { isOk() }
                content { json("[]") }
            }

        verify(chapterService).findAll(1L, 1L)
    }

    @Test
    fun `GET chapters should return not found when book does not exist`() {
        `when`(currentUser.getId()).thenReturn(1L)
        `when`(chapterService.findAll(999L, 1L))
            .thenThrow(NotFoundException("Book with ID 999 not found"))

        mockMvc.get("/api/books/999/chapters")
            .andExpect {
                status { isNotFound() }
            }

        verify(chapterService).findAll(999L, 1L)
    }

    // GET /api/books/{bookId}/chapters/{chapterId}

    @Test
    fun `GET chapter by id should return chapter when it exists`() {
        `when`(currentUser.getId()).thenReturn(1L)
        `when`(chapterService.findById(1L, 1L, 1L))
            .thenReturn(chapter1)

        mockMvc.get("/api/books/1/chapters/1")
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value(1) }
                jsonPath("$.bookId") { value(1) }
                jsonPath("$.title") { value("Introduction") }
                jsonPath("$.position") { value(1) }
            }

        verify(chapterService).findById(1L, 1L, 1L)
    }

    @Test
    fun `GET chapter by id should return not found when chapter does not exist`() {
        `when`(currentUser.getId()).thenReturn(1L)
        `when`(chapterService.findById(1L, 999L, 1L))
            .thenThrow(
                NotFoundException(
                    "Chapter with ID 999 not found in book with ID 1"
                )
            )

        mockMvc.get("/api/books/1/chapters/999")
            .andExpect {
                status { isNotFound() }
            }

        verify(chapterService).findById(1L, 999L, 1L)
    }

    // POST /api/books/{bookId}/chapters

    @Test
    fun `POST chapter should create and return chapter`() {
        val request = CreateChapterRequest(
            title = "Introduction",
            position = 1
        )

        `when`(currentUser.getId()).thenReturn(1L)
        `when`(chapterService.create(request, 1L, 1L))
            .thenReturn(chapter1)

        mockMvc.post("/api/books/1/chapters") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "title": "Introduction",
                    "position": 1
                }
            """.trimIndent()
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.bookId") { value(1) }
            jsonPath("$.title") { value("Introduction") }
            jsonPath("$.position") { value(1) }
        }

        verify(chapterService).create(request, 1L, 1L)
    }

    @Test
    fun `POST chapter should return bad request when title is blank`() {
        mockMvc.post("/api/books/1/chapters") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "title": "",
                    "position": 1
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `POST chapter should return bad request when position is below one`() {
        mockMvc.post("/api/books/1/chapters") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "title": "Introduction",
                    "position": 0
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
        }
    }

    // PUT /api/books/{bookId}/chapters/{chapterId}

    @Test
    fun `PUT chapter should update and return chapter`() {
        val request = UpdateChapterRequest(
            title = "Updated Introduction",
            position = 2
        )

        val updatedChapter = chapter1.copy(
            title = "Updated Introduction",
            position = 2
        )

        `when`(currentUser.getId()).thenReturn(1L)
        `when`(chapterService.update(1L, 1L, request, 1L))
            .thenReturn(updatedChapter)

        mockMvc.put("/api/books/1/chapters/1") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "title": "Updated Introduction",
                    "position": 2
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.bookId") { value(1) }
            jsonPath("$.title") { value("Updated Introduction") }
            jsonPath("$.position") { value(2) }
        }

        verify(chapterService).update(1L, 1L, request, 1L)
    }

    @Test
    fun `PUT chapter should return not found when chapter does not exist`() {
        val request = UpdateChapterRequest(
            title = "Updated Introduction",
            position = 2
        )

        `when`(currentUser.getId()).thenReturn(1L)
        `when`(chapterService.update(1L, 999L, request, 1L))
            .thenThrow(
                NotFoundException(
                    "Chapter with ID 999 not found in book with ID 1"
                )
            )

        mockMvc.put("/api/books/1/chapters/999") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "title": "Updated Introduction",
                    "position": 2
                }
            """.trimIndent()
        }.andExpect {
            status { isNotFound() }
        }

        verify(chapterService).update(1L, 999L, request, 1L)
    }

    @Test
    fun `PUT chapter should return bad request when title is blank`() {
        mockMvc.put("/api/books/1/chapters/1") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "title": "",
                    "position": 1
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `PUT chapter should return bad request when position is below one`() {
        mockMvc.put("/api/books/1/chapters/1") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "title": "Introduction",
                    "position": 0
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
        }
    }

    // DELETE /api/books/{bookId}/chapters/{chapterId}

    @Test
    fun `DELETE chapter should return no content when chapter is deleted`() {
        `when`(currentUser.getId()).thenReturn(1L)

        mockMvc.delete("/api/books/1/chapters/1")
            .andExpect {
                status { isNoContent() }
                content { string("") }
            }

        verify(chapterService).delete(1L, 1L, 1L)
    }

    @Test
    fun `DELETE chapter should return not found when chapter does not exist`() {
        `when`(currentUser.getId()).thenReturn(1L)

        doThrow(
            NotFoundException(
                "Chapter with ID 999 not found in book with ID 1"
            )
        ).`when`(chapterService).delete(1L, 999L, 1L)

        mockMvc.delete("/api/books/1/chapters/999")
            .andExpect {
                status { isNotFound() }
            }

        verify(chapterService).delete(1L, 999L, 1L)
    }
}
