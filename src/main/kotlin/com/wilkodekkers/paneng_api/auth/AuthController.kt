package com.wilkodekkers.paneng_api.auth

import com.wilkodekkers.paneng_api.auth.dto.AuthResponse
import com.wilkodekkers.paneng_api.auth.dto.LoginRequest
import com.wilkodekkers.paneng_api.auth.dto.RegisterRequest
import com.wilkodekkers.paneng_api.users.UserService
import com.wilkodekkers.paneng_api.users.dto.UserResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration and login")
class AuthController(
    private val userService: UserService,
    private val authService: AuthService,
) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(
        @Valid @RequestBody request: RegisterRequest
    ): UserResponse {
        return userService.create(
            name = request.name,
            email = request.email,
            password = request.password,
        )
    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest
    ): AuthResponse {
        return authService.login(request)
    }
}
