package com.wilkodekkers.paneng_api.security

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component

@Component
class CurrentUser {
    fun getId(): Long {
        val authentication = SecurityContextHolder.getContext().authentication

        val jwt = authentication as? JwtAuthenticationToken
            ?: throw IllegalStateException("No authenticated user found")

        return jwt.token.subject?.toLongOrNull()
            ?: throw IllegalStateException("Invalid JWT subject")
    }
}