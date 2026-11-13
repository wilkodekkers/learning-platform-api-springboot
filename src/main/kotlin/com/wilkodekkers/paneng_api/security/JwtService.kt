package com.wilkodekkers.paneng_api.security

import com.wilkodekkers.paneng_api.users.User
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class JwtService(
    private val jwtEncoder: JwtEncoder,
    private val jwtProperties: JwtProperties,
) {

    fun generateAccessToken(user: User): String {
        val now = Instant.now()

        val claims = JwtClaimsSet.builder()
            .issuer("paneng-api")
            .issuedAt(now)
            .expiresAt(now.plusMillis(jwtProperties.expiration))
            .subject(user.id.toString())
            .claim("email", user.email)
            .build()

        val header = JwsHeader.with(MacAlgorithm.HS256).build()

        return jwtEncoder
            .encode(JwtEncoderParameters.from(header, claims))
            .tokenValue
    }
}
