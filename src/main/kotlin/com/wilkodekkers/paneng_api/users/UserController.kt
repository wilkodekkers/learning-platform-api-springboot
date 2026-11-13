package com.wilkodekkers.paneng_api.users

import com.wilkodekkers.paneng_api.security.CurrentUser
import com.wilkodekkers.paneng_api.users.dto.UserResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Endpoints for managing user information")
@SecurityRequirement(name = "bearerAuth")
class UserController(
    private val userService: UserService,
    private val currentUser: CurrentUser
) {
    @GetMapping("/me")
    fun me(@AuthenticationPrincipal jwt: Jwt): UserResponse {
        return userService.findById(currentUser.getId())
    }
}