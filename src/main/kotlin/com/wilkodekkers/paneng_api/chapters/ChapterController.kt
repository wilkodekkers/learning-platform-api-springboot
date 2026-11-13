package com.wilkodekkers.paneng_api.chapters

import com.wilkodekkers.paneng_api.chapters.dto.ChapterResponse
import com.wilkodekkers.paneng_api.chapters.dto.CreateChapterRequest
import com.wilkodekkers.paneng_api.chapters.dto.UpdateChapterRequest
import com.wilkodekkers.paneng_api.security.CurrentUser
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/books/{bookId}/chapters")
@Tag(name = "Chapters", description = "Endpoints for book chapters")
@SecurityRequirement(name = "bearerAuth")
class ChapterController(
    private val chapterService: ChapterService,
    private val currentUser: CurrentUser,
) {
    @GetMapping
    fun findAll(@PathVariable bookId: Long): List<ChapterResponse> {
        return chapterService.findAll(bookId, currentUser.getId())
    }

    @GetMapping("/{chapterId}")
    fun findById(@PathVariable bookId: Long, @PathVariable chapterId: Long): ChapterResponse {
        return chapterService.findById(bookId, chapterId, currentUser.getId())
    }

    @PostMapping
    fun create(
        @PathVariable bookId: Long,
        @Valid @RequestBody request: CreateChapterRequest
    ): ChapterResponse {
        return chapterService.create(request, bookId, currentUser.getId())
    }

    @PutMapping("/{chapterId}")
    fun update(
        @PathVariable bookId: Long,
        @PathVariable chapterId: Long,
        @Valid @RequestBody request: UpdateChapterRequest
    ): ChapterResponse {
        return chapterService.update(bookId, chapterId, request, currentUser.getId())
    }

    @DeleteMapping("/{chapterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable bookId: Long,
        @PathVariable chapterId: Long,
    ) {
        chapterService.delete(bookId, chapterId, currentUser.getId())
    }
}
