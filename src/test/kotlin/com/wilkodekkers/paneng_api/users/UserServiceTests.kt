
package com.wilkodekkers.paneng_api.users

import com.wilkodekkers.paneng_api.common.exceptions.ConflictException
import com.wilkodekkers.paneng_api.common.exceptions.NotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Instant
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class UserServiceTests {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    private lateinit var userService: UserService

    private val user = User(
        id = 1L,
        name = "Test User",
        email = "test@example.com",
        password = "encoded-password",
        createdAt = Instant.parse("2026-01-01T10:00:00Z"),
        updatedAt = Instant.parse("2026-01-02T10:00:00Z")
    )

    @BeforeEach
    fun setUp() {
        userService = UserService(
            userRepository = userRepository,
            passwordEncoder = passwordEncoder
        )
    }

    @Test
    fun `create saves user and returns response when email is available`() {
        Mockito.`when`(userRepository.existsByEmail(user.email))
            .thenReturn(false)

        Mockito.`when`(passwordEncoder.encode("password123"))
            .thenReturn("encoded-password")

        var savedUser: User? = null

        Mockito.`when`(userRepository.save(Mockito.any(User::class.java)))
            .thenAnswer { invocation ->
                val newUser = invocation.getArgument<User>(0)

                savedUser = User(
                    id = 1L,
                    name = newUser.name,
                    email = newUser.email,
                    password = newUser.password,
                    createdAt = newUser.createdAt,
                    updatedAt = newUser.updatedAt
                )

                savedUser
            }

        val response = userService.create(
            name = user.name,
            email = user.email,
            password = "password123"
        )

        assertEquals(1L, response.id)
        assertEquals(user.name, response.name)
        assertEquals(user.email, response.email)
        assertInstanceOf<User>(savedUser)
        assertEquals(savedUser.createdAt, response.createdAt)
        assertEquals(savedUser.updatedAt, response.updatedAt)

        verify(userRepository).existsByEmail(user.email)
        verify(passwordEncoder).encode("password123")
        verify(userRepository).save(Mockito.any(User::class.java))
    }

    @Test
    fun `create throws conflict exception when email is already registered`() {
        Mockito.`when`(userRepository.existsByEmail(user.email))
            .thenReturn(true)

        assertThrows(ConflictException::class.java) {
            userService.create(
                name = user.name,
                email = user.email,
                password = "password123"
            )
        }

        verify(userRepository).existsByEmail(user.email)
        verifyNoInteractions(passwordEncoder)
    }

    @Test
    fun `create throws illegal state exception when password encoding fails`() {
        Mockito.`when`(userRepository.existsByEmail(user.email))
            .thenReturn(false)

        Mockito.`when`(passwordEncoder.encode("password123"))
            .thenReturn(null)

        assertThrows(IllegalStateException::class.java) {
            userService.create(
                name = user.name,
                email = user.email,
                password = "password123"
            )
        }

        verify(userRepository).existsByEmail(user.email)
        verify(passwordEncoder).encode("password123")
        Mockito.verify(userRepository, Mockito.never())
            .save(Mockito.any(User::class.java))
    }

    @Test
    fun `findById returns user when user exists`() {
        Mockito.`when`(userRepository.findById(1L))
            .thenReturn(Optional.of(user))

        val response = userService.findById(1L)

        assertEquals(user.id, response.id)
        assertEquals(user.name, response.name)
        assertEquals(user.email, response.email)
        assertEquals(user.createdAt, response.createdAt)
        assertEquals(user.updatedAt, response.updatedAt)

        verify(userRepository).findById(1L)
    }

    @Test
    fun `findById throws not found exception when user does not exist`() {
        Mockito.`when`(userRepository.findById(99L))
            .thenReturn(Optional.empty())

        assertThrows(NotFoundException::class.java) {
            userService.findById(99L)
        }

        verify(userRepository).findById(99L)
    }
}
