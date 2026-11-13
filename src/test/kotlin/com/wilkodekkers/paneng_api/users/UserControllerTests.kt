
package com.wilkodekkers.paneng_api.users

import com.wilkodekkers.paneng_api.common.exceptions.NotFoundException
import com.wilkodekkers.paneng_api.security.CurrentUser
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.Instant

@WebMvcTest(
    controllers = [UserController::class],
    excludeAutoConfiguration = [
        SecurityAutoConfiguration::class,
        OAuth2ResourceServerAutoConfiguration::class
    ]
)
class UserControllerTests(
    @Autowired private val mockMvc: MockMvc
) {

    @MockitoBean
    private lateinit var userService: UserService

    @MockitoBean
    private lateinit var currentUser: CurrentUser

    @Test
    fun `me returns authenticated user profile`() {
        val userResponse = com.wilkodekkers.paneng_api.users.dto.UserResponse(
            id = 1L,
            name = "Test User",
            email = "test@example.com",
            createdAt = Instant.parse("2026-01-01T10:00:00Z"),
            updatedAt = Instant.parse("2026-01-02T10:00:00Z")
        )

        Mockito.`when`(currentUser.getId()).thenReturn(1L)
        Mockito.`when`(userService.findById(1L))
            .thenReturn(userResponse)

        mockMvc.get("/api/users/me")
            .andExpect {
                status { isOk() }
                content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                jsonPath("$.id") { value(1) }
                jsonPath("$.name") { value("Test User") }
                jsonPath("$.email") { value("test@example.com") }
                jsonPath("$.password").doesNotExist()
            }

        verify(currentUser).getId()
        verify(userService).findById(1L)
    }

    @Test
    fun `me returns not found when user does not exist`() {
        Mockito.`when`(currentUser.getId()).thenReturn(99L)

        Mockito.`when`(userService.findById(99L))
            .thenThrow(NotFoundException("User not found"))

        mockMvc.get("/api/users/me")
            .andExpect {
                status { isNotFound() }
            }

        verify(currentUser).getId()
        verify(userService).findById(99L)
    }
}
