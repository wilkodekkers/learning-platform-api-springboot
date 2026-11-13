package com.wilkodekkers.paneng_api.books

import com.wilkodekkers.paneng_api.books.dto.BookResponse
import com.wilkodekkers.paneng_api.books.dto.CreateBookRequest
import com.wilkodekkers.paneng_api.books.dto.UpdateBookRequest
import com.wilkodekkers.paneng_api.security.CurrentUser
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
@RequestMapping("/api/books")
class BookController(
    private val bookService: BookService,
    private val currentUser: CurrentUser,
) {
    @GetMapping
    fun findAll(): List<BookResponse> {
        return bookService.findAll(currentUser.getId())
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): BookResponse {
        return bookService.findById(id, currentUser.getId())
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateBookRequest): BookResponse {
        return bookService.create(currentUser.getId(), request)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateBookRequest
    ): BookResponse {
        return bookService.update(id, currentUser.getId(), request)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        bookService.delete(id, currentUser.getId())
    }
}