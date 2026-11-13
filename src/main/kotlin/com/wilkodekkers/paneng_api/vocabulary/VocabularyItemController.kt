package com.wilkodekkers.paneng_api.vocabulary

import com.wilkodekkers.paneng_api.security.CurrentUser
import com.wilkodekkers.paneng_api.vocabulary.dto.CreateVocabularyItemRequest
import com.wilkodekkers.paneng_api.vocabulary.dto.UpdateVocabularyItemRequest
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/books/{bookId}/chapters/{chapterId}/vocabulary-items")
@Tag(name = "Vocabulary items", description = "Endpoints for managing book vocabulary items")
@SecurityRequirement(name = "bearerAuth")
class VocabularyItemController(
    private val vocabularyItemService: VocabularyItemService,
    private val currentUser: CurrentUser,
) {

    @GetMapping
    fun findAll(
        @PathVariable bookId: Long,
        @PathVariable chapterId: Long
    ) = vocabularyItemService.findAll(
        bookId,
        chapterId,
        currentUser.getId()
    )

    @GetMapping("/{vocabularyItemId}")
    fun findById(
        @PathVariable bookId: Long,
        @PathVariable chapterId: Long,
        @PathVariable vocabularyItemId: Long
    ) = vocabularyItemService.findById(
        bookId,
        chapterId,
        vocabularyItemId,
        currentUser.getId()
    )

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable bookId: Long,
        @PathVariable chapterId: Long,
        @Valid @RequestBody request: CreateVocabularyItemRequest
    ) = vocabularyItemService.create(
        bookId,
        chapterId,
        currentUser.getId(),
        request
    )

    @PutMapping("/{vocabularyItemId}")
    fun update(
        @PathVariable bookId: Long,
        @PathVariable chapterId: Long,
        @PathVariable vocabularyItemId: Long,
        @Valid @RequestBody request: UpdateVocabularyItemRequest
    ) = vocabularyItemService.update(
        bookId,
        chapterId,
        vocabularyItemId,
        currentUser.getId(),
        request
    )

    @DeleteMapping("/{vocabularyItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable bookId: Long,
        @PathVariable chapterId: Long,
        @PathVariable vocabularyItemId: Long
    ) = vocabularyItemService.delete(
        bookId,
        chapterId,
        vocabularyItemId,
        currentUser.getId()
    )
}
