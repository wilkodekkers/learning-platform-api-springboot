package com.wilkodekkers.paneng_api.auth

import com.wilkodekkers.paneng_api.auth.dto.AuthResponse
import com.wilkodekkers.paneng_api.auth.dto.LoginRequest
import com.wilkodekkers.paneng_api.common.exceptions.NotFoundException
import com.wilkodekkers.paneng_api.security.JwtProperties
import com.wilkodekkers.paneng_api.security.JwtService
import com.wilkodekkers.paneng_api.users.UserRepository
import com.wilkodekkers.paneng_api.users.dto.UserResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val jwtProperties: JwtProperties,
) {

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): AuthResponse {

        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated(
                request.email,
                request.password
            )
        )

        val user = userRepository.findByEmail(request.email)
            ?: throw NotFoundException("User not found")

        val accessToken = jwtService.generateAccessToken(user)

        val userResponse = UserResponse(
            id = user.id,
            name = user.name,
            email = user.email,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt,
        )

        return AuthResponse(
            accessToken = accessToken,
            tokenType = "Bearer",
            expiresIn = jwtProperties.expiration / 1000,
            user = userResponse,
        )
    }
}
