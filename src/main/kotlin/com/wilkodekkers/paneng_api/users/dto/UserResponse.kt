package com.wilkodekkers.paneng_api.users.dto

import java.time.Instant

data class UserResponse(
    val id: Long,
    val name: String,
    val email: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
