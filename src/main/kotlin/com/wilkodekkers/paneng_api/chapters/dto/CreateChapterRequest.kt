package com.wilkodekkers.paneng_api.chapters.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateChapterRequest(
    @field:NotBlank(message = "Title is required")
    @field:Size(
        max = 255,
        message = "Title cannot exceed 255 characters"
    )
    val title: String,

    @field:Min(value = 1, message = "Position must be at least 1")
    val position: Int = 1
)
