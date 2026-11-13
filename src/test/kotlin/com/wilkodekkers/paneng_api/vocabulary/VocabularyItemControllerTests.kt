package com.wilkodekkers.paneng_api.vocabulary

import com.wilkodekkers.paneng_api.common.exceptions.NotFoundException
import com.wilkodekkers.paneng_api.security.CurrentUser
import com.wilkodekkers.paneng_api.vocabulary.dto.CreateVocabularyItemRequest
import com.wilkodekkers.paneng_api.vocabulary.dto.UpdateVocabularyItemRequest
import com.wilkodekkers.paneng_api.vocabulary.dto.VocabularyItemResponse
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
    controllers = [VocabularyItemController::class],
    excludeAutoConfiguration = [
        SecurityAutoConfiguration::class,
        OAuth2ResourceServerAutoConfiguration::class
    ]
)
class VocabularyItemControllerTests(
    @Autowired private val mockMvc: MockMvc
) {

    @MockitoBean
    private lateinit var vocabularyItemService: VocabularyItemService

    @MockitoBean
    private lateinit var currentUser: CurrentUser

    private val vocabularyItem1 = VocabularyItemResponse(
        id = 1L,
        chapterId = 1L,
        term = "กิน",
        definition = "To eat",
        createdAt = Instant.parse("2025-01-01T10:00:00Z"),
        updatedAt = Instant.parse("2025-01-01T10:00:00Z")
    )

    private val vocabularyItem2 = VocabularyItemResponse(
        id = 2L,
        chapterId = 1L,
        term = "ดื่ม",
        definition = "To drink",
        createdAt = Instant.parse("2025-01-02T10:00:00Z"),
        updatedAt = Instant.parse("2025-01-02T10:00:00Z")
    )

    // GET /api/books/{bookId}/chapters/{chapterId}/vocabulary-items

    @Test
    fun `GET vocabulary items should return all items belonging to chapter`() {
        `when`(currentUser.getId()).thenReturn(1L)
        `when`(vocabularyItemService.findAll(1L, 1L, 1L))
            .thenReturn(listOf(vocabularyItem1, vocabularyItem2))

        mockMvc.get("/api/books/1/chapters/1/vocabulary-items")
            .andExpect {
                status { isOk() }
                jsonPath("$.length()") { value(2) }
                jsonPath("$[0].id") { value(1) }
                jsonPath("$[0].chapterId") { value(1) }
                jsonPath("$[0].term") { value("กิน") }
                jsonPath("$[0].definition") { value("To eat") }
                jsonPath("$[1].id") { value(2) }
                jsonPath("$[1].term") { value("ดื่ม") }
                jsonPath("$[1].definition") { value("To drink") }
            }

        verify(vocabularyItemService).findAll(1L, 1L, 1L)
    }

    @Test
    fun `GET vocabulary items should return empty list when chapter has no items`() {
        `when`(currentUser.getId()).thenReturn(1L)
        `when`(vocabularyItemService.findAll(1L, 1L, 1L))
            .thenReturn(emptyList())

        mockMvc.get("/api/books/1/chapters/1/vocabulary-items")
            .andExpect {
                status { isOk() }
                content { json("[]") }
            }

        verify(vocabularyItemService).findAll(1L, 1L, 1L)
    }

    @Test
    fun `GET vocabulary items should return not found when chapter does not exist`() {
        `when`(currentUser.getId()).thenReturn(1L)
        `when`(vocabularyItemService.findAll(1L, 999L, 1L))
            .thenThrow(
                NotFoundException(
                    "Chapter with ID 999 not found in book with ID 1"
                )
            )

        mockMvc.get("/api/books/1/chapters/999/vocabulary-items")
            .andExpect {
                status { isNotFound() }
            }

        verify(vocabularyItemService).findAll(1L, 999L, 1L)
    }

    // GET /api/books/{bookId}/chapters/{chapterId}/vocabulary-items/{vocabularyItemId}

    @Test
    fun `GET vocabulary item by id should return item when it exists`() {
        `when`(currentUser.getId()).thenReturn(1L)
        `when`(vocabularyItemService.findById(1L, 1L, 1L, 1L))
            .thenReturn(vocabularyItem1)

        mockMvc.get("/api/books/1/chapters/1/vocabulary-items/1")
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value(1) }
                jsonPath("$.chapterId") { value(1) }
                jsonPath("$.term") { value("กิน") }
                jsonPath("$.definition") { value("To eat") }
            }

        verify(vocabularyItemService).findById(1L, 1L, 1L, 1L)
    }

    @Test
    fun `GET vocabulary item by id should return not found when item does not exist`() {
        `when`(currentUser.getId()).thenReturn(1L)
        `when`(vocabularyItemService.findById(1L, 1L, 999L, 1L))
            .thenThrow(
                NotFoundException(
                    "Vocabulary item with ID 999 not found"
                )
            )

        mockMvc.get("/api/books/1/chapters/1/vocabulary-items/999")
            .andExpect {
                status { isNotFound() }
            }

        verify(vocabularyItemService).findById(1L, 1L, 999L, 1L)
    }

    // POST /api/books/{bookId}/chapters/{chapterId}/vocabulary-items

    @Test
    fun `POST vocabulary item should create and return item`() {
        val request = CreateVocabularyItemRequest(
            term = "กิน",
            definition = "To eat"
        )

        `when`(currentUser.getId()).thenReturn(1L)
        `when`(vocabularyItemService.create(1L, 1L, 1L, request))
            .thenReturn(vocabularyItem1)

        mockMvc.post("/api/books/1/chapters/1/vocabulary-items") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "term": "กิน",
                    "definition": "To eat"
                }
            """.trimIndent()
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.chapterId") { value(1) }
            jsonPath("$.term") { value("กิน") }
            jsonPath("$.definition") { value("To eat") }
        }

        verify(vocabularyItemService).create(1L, 1L, 1L, request)
    }

    @Test
    fun `POST vocabulary item should return bad request when term is blank`() {
        mockMvc.post("/api/books/1/chapters/1/vocabulary-items") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "term": "",
                    "definition": "To eat"
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `POST vocabulary item should return bad request when definition is blank`() {
        mockMvc.post("/api/books/1/chapters/1/vocabulary-items") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "term": "กิน",
                    "definition": ""
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `POST vocabulary item should return bad request when term exceeds 255 characters`() {
        val longTerm = "a".repeat(256)

        mockMvc.post("/api/books/1/chapters/1/vocabulary-items") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "term": "$longTerm",
                    "definition": "To eat"
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
        }
    }

    // PUT /api/books/{bookId}/chapters/{chapterId}/vocabulary-items/{vocabularyItemId}

    @Test
    fun `PUT vocabulary item should update and return item`() {
        val request = UpdateVocabularyItemRequest(
            term = "รับประทาน",
            definition = "To eat (polite)"
        )

        val updatedItem = vocabularyItem1.copy(
            term = "รับประทาน",
            definition = "To eat (polite)"
        )

        `when`(currentUser.getId()).thenReturn(1L)
        `when`(vocabularyItemService.update(1L, 1L, 1L, 1L, request))
            .thenReturn(updatedItem)

        mockMvc.put("/api/books/1/chapters/1/vocabulary-items/1") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "term": "รับประทาน",
                    "definition": "To eat (polite)"
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.chapterId") { value(1) }
            jsonPath("$.term") { value("รับประทาน") }
            jsonPath("$.definition") { value("To eat (polite)") }
        }

        verify(vocabularyItemService).update(1L, 1L, 1L, 1L, request)
    }

    @Test
    fun `PUT vocabulary item should return not found when item does not exist`() {
        val request = UpdateVocabularyItemRequest(
            term = "รับประทาน",
            definition = "To eat (polite)"
        )

        `when`(currentUser.getId()).thenReturn(1L)
        `when`(vocabularyItemService.update(1L, 1L, 999L, 1L, request))
            .thenThrow(
                NotFoundException(
                    "Vocabulary item with ID 999 not found"
                )
            )

        mockMvc.put("/api/books/1/chapters/1/vocabulary-items/999") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "term": "รับประทาน",
                    "definition": "To eat (polite)"
                }
            """.trimIndent()
        }.andExpect {
            status { isNotFound() }
        }

        verify(vocabularyItemService).update(1L, 1L, 999L, 1L, request)
    }

    @Test
    fun `PUT vocabulary item should return bad request when term is blank`() {
        mockMvc.put("/api/books/1/chapters/1/vocabulary-items/1") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "term": "",
                    "definition": "To eat"
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `PUT vocabulary item should return bad request when definition is blank`() {
        mockMvc.put("/api/books/1/chapters/1/vocabulary-items/1") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "term": "กิน",
                    "definition": ""
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `PUT vocabulary item should return bad request when term exceeds 255 characters`() {
        val longTerm = "a".repeat(256)

        mockMvc.put("/api/books/1/chapters/1/vocabulary-items/1") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "term": "$longTerm",
                    "definition": "To eat"
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
        }
    }

    // DELETE /api/books/{bookId}/chapters/{chapterId}/vocabulary-items/{vocabularyItemId}

    @Test
    fun `DELETE vocabulary item should return no content when item is deleted`() {
        `when`(currentUser.getId()).thenReturn(1L)

        mockMvc.delete("/api/books/1/chapters/1/vocabulary-items/1")
            .andExpect {
                status { isNoContent() }
                content { string("") }
            }

        verify(vocabularyItemService).delete(1L, 1L, 1L, 1L)
    }

    @Test
    fun `DELETE vocabulary item should return not found when item does not exist`() {
        `when`(currentUser.getId()).thenReturn(1L)

        doThrow(
            NotFoundException("Vocabulary item with ID 999 not found")
        ).`when`(vocabularyItemService).delete(1L, 1L, 999L, 1L)

        mockMvc.delete("/api/books/1/chapters/1/vocabulary-items/999")
            .andExpect {
                status { isNotFound() }
            }

        verify(vocabularyItemService).delete(1L, 1L, 999L, 1L)
    }
}
