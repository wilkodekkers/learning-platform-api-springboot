package com.wilkodekkers.paneng_api.auth

import com.wilkodekkers.paneng_api.auth.dto.LoginRequest
import com.wilkodekkers.paneng_api.common.exceptions.NotFoundException
import com.wilkodekkers.paneng_api.security.JwtProperties
import com.wilkodekkers.paneng_api.security.JwtService
import com.wilkodekkers.paneng_api.users.User
import com.wilkodekkers.paneng_api.users.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class AuthServiceTests {

    @Mock
    private lateinit var authenticationManager: AuthenticationManager

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var jwtService: JwtService

    private lateinit var authService: AuthService

    private val jwtProperties = JwtProperties(
        secret = "test-secret",
        expiration = 900_000L
    )

    private val email = "test@example.com"
    private val password = "password123"

    private val user = User(
        id = 1L,
        name = "Test User",
        email = email,
        password = "hashed-password",
        createdAt = Instant.parse("2026-01-01T10:00:00Z"),
        updatedAt = Instant.parse("2026-01-02T10:00:00Z")
    )

    @BeforeEach
    fun setUp() {
        authService = AuthService(
            authenticationManager = authenticationManager,
            userRepository = userRepository,
            jwtService = jwtService,
            jwtProperties = jwtProperties
        )
    }

    @Test
    fun `login returns authentication response when credentials are valid`() {
        val request = LoginRequest(
            email = email,
            password = password
        )

        val accessToken = "test-access-token"

        Mockito.`when`(userRepository.findByEmail(email))
            .thenReturn(user)

        Mockito.`when`(jwtService.generateAccessToken(user))
            .thenReturn(accessToken)

        val response = authService.login(request)

        assertEquals(accessToken, response.accessToken)
        assertEquals("Bearer", response.tokenType)
        assertEquals(900L, response.expiresIn)

        assertEquals(user.id, response.user.id)
        assertEquals(user.name, response.user.name)
        assertEquals(user.email, response.user.email)
        assertEquals(user.createdAt, response.user.createdAt)
        assertEquals(user.updatedAt, response.user.updatedAt)

        verify(authenticationManager).authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated(
                email,
                password
            )
        )

        verify(userRepository).findByEmail(email)
        verify(jwtService).generateAccessToken(user)
    }

    @Test
    fun `login throws exception when authentication fails`() {
        val request = LoginRequest(
            email = email,
            password = password
        )

        Mockito.`when`(
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                    email,
                    password
                )
            )
        ).thenThrow(BadCredentialsException("Bad credentials"))

        assertThrows(BadCredentialsException::class.java) {
            authService.login(request)
        }

        Mockito.verifyNoInteractions(userRepository, jwtService)
    }

    @Test
    fun `login throws exception when authenticated user cannot be found`() {
        val request = LoginRequest(
            email = email,
            password = password
        )

        Mockito.`when`(userRepository.findByEmail(email))
            .thenReturn(null)

        assertThrows(NotFoundException::class.java) {
            authService.login(request)
        }

        verify(authenticationManager).authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated(
                email,
                password
            )
        )

        Mockito.verifyNoInteractions(jwtService)
    }
}
