package com.wilkodekkers.paneng_api.users

import com.wilkodekkers.paneng_api.users.dto.UserResponse
import com.wilkodekkers.paneng_api.common.exceptions.ConflictException
import com.wilkodekkers.paneng_api.common.exceptions.NotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional
    fun create(
        name: String,
        email: String,
        password: String
    ): UserResponse {

        if (userRepository.existsByEmail(email)) {
            throw ConflictException("Email address is already registered")
        }

        val encodedPassword = passwordEncoder.encode(password)
            ?: throw IllegalStateException("Password encoding failed")

        val user = User(
            name = name,
            email = email,
            password = encodedPassword,
        )

        return userRepository.save(user).toResponse()
    }

    @Transactional(readOnly = true)
    fun findById(userId: Long): UserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { NotFoundException("User not found") }

        return user.toResponse()
    }

    private fun User.toResponse() = UserResponse(
        id = id,
        name = name,
        email = email,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
