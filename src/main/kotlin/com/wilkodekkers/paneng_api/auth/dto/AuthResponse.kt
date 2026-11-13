package com.wilkodekkers.paneng_api.auth.dto

import com.wilkodekkers.paneng_api.users.dto.UserResponse

data class AuthResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Long,
    val user: UserResponse,
)
