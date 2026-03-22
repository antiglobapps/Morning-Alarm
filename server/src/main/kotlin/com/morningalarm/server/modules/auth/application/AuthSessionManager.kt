package com.morningalarm.server.modules.auth.application

import com.morningalarm.server.modules.auth.application.ports.AccessTokenService
import com.morningalarm.server.modules.auth.application.ports.AuthUserRepository
import com.morningalarm.server.modules.auth.application.ports.BusinessUserRepository
import com.morningalarm.server.modules.auth.application.ports.Clock
import com.morningalarm.server.modules.auth.application.ports.TokenFactory
import com.morningalarm.server.modules.auth.domain.AuthSession
import com.morningalarm.server.modules.auth.domain.RefreshTokenRecord
import com.morningalarm.server.modules.auth.domain.UserRole
import com.morningalarm.server.shared.errors.ValidationException

class AuthSessionManager(
    private val authUserRepository: AuthUserRepository,
    private val businessUserRepository: BusinessUserRepository,
    private val tokenFactory: TokenFactory,
    private val accessTokenService: AccessTokenService,
    private val clock: Clock,
    private val adminEmails: Set<String>,
    private val accessTokenTtlSeconds: Long,
    private val refreshTokenTtlSeconds: Long,
) {
    fun issueSession(userId: String, role: UserRole, isNewUser: Boolean): AuthSession {
        val now = epochSeconds()
        val expiresAt = now + accessTokenTtlSeconds
        val refreshToken = tokenFactory.newRefreshToken()
        authUserRepository.saveRefreshToken(
            RefreshTokenRecord(
                token = refreshToken,
                userId = userId,
                expiresAtEpochSeconds = now + refreshTokenTtlSeconds,
            ),
        )
        return AuthSession(
            userId = userId,
            role = role,
            bearerToken = accessTokenService.issueToken(userId, role, expiresAt),
            refreshToken = refreshToken,
            expiresAtEpochSeconds = expiresAt,
            isNewUser = isNewUser,
        )
    }

    fun ensureBusinessUser(id: String, email: String?, displayName: String?) =
        businessUserRepository.ensureUser(
            id = id,
            email = email,
            displayName = displayName,
            role = businessUserRepository.findById(id)?.role ?: roleForEmail(email),
        )

    fun roleForEmail(email: String?): UserRole {
        return if (email != null && adminEmails.contains(email.trim().lowercase())) {
            UserRole.ADMIN
        } else {
            UserRole.USER
        }
    }

    fun normalizeEmail(email: String): String {
        val normalized = email.trim().lowercase()
        if (normalized.isBlank() || !normalized.contains("@")) {
            throw ValidationException("Email must be valid")
        }
        return normalized
    }

    fun validatePassword(password: String) {
        if (password.length < 8) {
            throw ValidationException("Password must contain at least 8 characters")
        }
    }

    fun requireOpaqueToken(token: String) {
        if (token.isBlank()) {
            throw ValidationException("Token must not be blank")
        }
    }

    fun epochSeconds(): Long = clock.epochSeconds()
}
