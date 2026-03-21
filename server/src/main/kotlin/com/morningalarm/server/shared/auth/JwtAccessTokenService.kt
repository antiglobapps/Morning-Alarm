package com.morningalarm.server.shared.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import com.morningalarm.server.modules.auth.application.ports.AccessTokenService
import com.morningalarm.server.modules.auth.domain.AccessTokenPayload
import com.morningalarm.server.modules.auth.domain.UserRole
import java.time.Instant
import java.util.Date

class JwtAccessTokenService(
    secret: String,
    issuer: String,
    audience: String,
) : AccessTokenService {
    private val algorithm = Algorithm.HMAC256(secret)
    private val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()
    private val tokenIssuer = issuer
    private val tokenAudience = audience

    override fun issueToken(userId: String, role: UserRole, expiresAtEpochSeconds: Long): String {
        return JWT.create()
            .withIssuer(tokenIssuer)
            .withAudience(tokenAudience)
            .withSubject(userId)
            .withClaim("role", role.name)
            .withExpiresAt(Date.from(Instant.ofEpochSecond(expiresAtEpochSeconds)))
            .sign(algorithm)
    }

    override fun verify(token: String): AccessTokenPayload? {
        return runCatching {
            val decoded = verifier.verify(token)
            AccessTokenPayload(
                userId = decoded.subject,
                role = UserRole.valueOf(decoded.getClaim("role").asString()),
                expiresAtEpochSeconds = decoded.expiresAtAsInstant.epochSecond,
            )
        }.getOrNull()
    }
}
