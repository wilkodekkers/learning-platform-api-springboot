
package com.wilkodekkers.paneng_api.auth

import com.wilkodekkers.paneng_api.auth.dto.AuthResponse
import com.wilkodekkers.paneng_api.auth.dto.LoginRequest
import com.wilkodekkers.paneng_api.users.UserService
import com.wilkodekkers.paneng_api.users.dto.UserResponse
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.Instant

@WebMvcTest(
    controllers = [AuthController::class],
    excludeAutoConfiguration = [
        SecurityAutoConfiguration::class,
        OAuth2ResourceServerAutoConfiguration::class
    ]
)
class AuthControllerTests(
    @Autowired private val mockMvc: MockMvc
) {

    @MockitoBean
    private lateinit var userService: UserService

    @MockitoBean
    private lateinit var authService: AuthService

    private val userResponse = UserResponse(
        id = 1L,
        name = "Test User",
        email = "test@example.com",
        createdAt = Instant.parse("2026-01-01T10:00:00Z"),
        updatedAt = Instant.parse("2026-01-02T10:00:00Z")
    )

    @Test
    fun `register returns created user when request is valid`() {
        Mockito.`when`(
            userService.create(
                name = "Test User",
                email = "test@example.com",
                password = "password123"
            )
        ).thenReturn(userResponse)

        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "name": "Test User",
                    "email": "test@example.com",
                    "password": "password123"
                }
            """.trimIndent()
        }.andExpect {
            status { isCreated() }
            content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
            jsonPath("$.id") { value(1) }
            jsonPath("$.name") { value("Test User") }
            jsonPath("$.email") { value("test@example.com") }
            jsonPath("$.password").doesNotExist()
        }

        verify(userService).create(
            name = "Test User",
            email = "test@example.com",
            password = "password123"
        )

        verifyNoInteractions(authService)
    }

    @Test
    fun `register returns bad request when name is blank`() {
        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "name": "",
                    "email": "test@example.com",
                    "password": "password123"
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
        }

        verifyNoInteractions(userService, authService)
    }

    @Test
    fun `register returns bad request when email is invalid`() {
        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "name": "Test User",
                    "email": "invalid-email",
                    "password": "password123"
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
        }

        verifyNoInteractions(userService, authService)
    }

    @Test
    fun `register returns bad request when password is too short`() {
        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "name": "Test User",
                    "email": "test@example.com",
                    "password": "short"
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
        }

        verifyNoInteractions(userService, authService)
    }

    @Test
    fun `login returns authentication response when request is valid`() {
        val authResponse = AuthResponse(
            accessToken = "test-access-token",
            tokenType = "Bearer",
            expiresIn = 900L,
            user = userResponse
        )

        Mockito.`when`(
            authService.login(
                LoginRequest(
                    email = "test@example.com",
                    password = "password123"
                )
            )
        ).thenReturn(authResponse)

        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "email": "test@example.com",
                    "password": "password123"
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
            content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
            jsonPath("$.accessToken") { value("test-access-token") }
            jsonPath("$.tokenType") { value("Bearer") }
            jsonPath("$.expiresIn") { value(900) }
            jsonPath("$.user.id") { value(1) }
            jsonPath("$.user.email") { value("test@example.com") }
        }

        verify(authService).login(
            LoginRequest(
                email = "test@example.com",
                password = "password123"
            )
        )

        verifyNoInteractions(userService)
    }

    @Test
    fun `login returns bad request when email is invalid`() {
        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "email": "invalid-email",
                    "password": "password123"
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
        }

        verifyNoInteractions(userService, authService)
    }

    @Test
    fun `login returns bad request when password is blank`() {
        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                    "email": "test@example.com",
                    "password": ""
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
        }

        verifyNoInteractions(userService, authService)
    }
}
