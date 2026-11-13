package com.wilkodekkers.paneng_api.books.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateBookRequest(
    @field:NotBlank(message = "Title is required")
    @field:Size(
        max = 255,
        message = "Title cannot exceed 255 characters"
    )
    val title: String,

    @field:NotBlank(message = "Author is required")
    @field:Size(
        max = 255,
        message = "Author cannot exceed 255 characters"
    )
    val author: String,
)
