package com.wilkodekkers.paneng_api.books

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface BookRepository : JpaRepository<Book, Long> {
    fun findAllByUserId(userId: Long): List<Book>
    fun findByIdAndUserId(id: Long, userId: Long): Optional<Book>
}
