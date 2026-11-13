package com.wilkodekkers.paneng_api.books

import com.wilkodekkers.paneng_api.books.dto.BookResponse
import com.wilkodekkers.paneng_api.books.dto.CreateBookRequest
import com.wilkodekkers.paneng_api.books.dto.UpdateBookRequest
import com.wilkodekkers.paneng_api.common.exceptions.NotFoundException
import com.wilkodekkers.paneng_api.users.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class BookService(
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
) {
    @Transactional(readOnly = true)
    fun findAll(userId: Long): List<BookResponse> {
        return bookRepository
            .findAllByUserId(userId)
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun findById(bookId: Long, userId: Long): BookResponse {
        return findBookByIdAndUserId(bookId, userId).toResponse()
    }

    @Transactional
    fun create(userId: Long, request: CreateBookRequest): BookResponse {
        val user = userRepository.getReferenceById(userId)

        val book = Book(
            title = request.title,
            author = request.author,
            user = user,
        )

        return bookRepository.save(book).toResponse()
    }

    @Transactional
    fun update(bookId: Long, userId: Long, request: UpdateBookRequest): BookResponse {
        val book = findBookByIdAndUserId(bookId, userId)

        book.title = request.title
        book.author = request.author
        book.updatedAt = Instant.now()

        return book.toResponse()
    }

    @Transactional
    fun delete(bookId: Long, userId: Long) {
        val book = findBookByIdAndUserId(bookId, userId)

        bookRepository.delete(book)
    }

    private fun findBookByIdAndUserId(bookId: Long, userId: Long): Book {
        return bookRepository.findByIdAndUserId(bookId, userId)
            .orElseThrow {
                NotFoundException("Book with ID $bookId not found")
            }
    }

    private fun Book.toResponse() = BookResponse(
        id = id,
        title = title,
        author = author,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
