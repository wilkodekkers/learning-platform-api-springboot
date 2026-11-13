package com.wilkodekkers.paneng_api.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.security.jwt")
data class JwtProperties(
    val secret: String,
    val expiration: Long,
)
