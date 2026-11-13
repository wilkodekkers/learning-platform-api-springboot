package com.wilkodekkers.paneng_api.vocabulary.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UpdateVocabularyItemRequest(
    @field:NotBlank
    @field:Size(max = 255)
    val term: String,

    @field:NotBlank
    val definition: String,
)
