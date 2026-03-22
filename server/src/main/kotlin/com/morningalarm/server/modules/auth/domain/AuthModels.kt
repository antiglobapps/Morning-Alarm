package com.morningalarm.server.modules.auth.domain

import com.morningalarm.Email
import com.morningalarm.EpochSeconds
import com.morningalarm.UserId

data class AuthUser(
    val id: UserId,
    val email: Email?,
    val displayName: String?,
    val passwordHash: String?,
    val socialAccounts: Set<SocialAccount>,
)

data class SocialAccount(
    val provider: SocialProvider,
    val externalSubject: String,
)

enum class SocialProvider {
    GOOGLE,
    VK,
    FACEBOOK,
    APPLE,
}

data class AuthSession(
    val userId: UserId,
    val role: UserRole,
    val bearerToken: String,
    val refreshToken: String,
    val expiresAtEpochSeconds: EpochSeconds,
    val isNewUser: Boolean,
)

enum class UserRole {
    ADMIN,
    USER,
}

data class RefreshTokenRecord(
    val token: String,
    val userId: UserId,
    val expiresAtEpochSeconds: EpochSeconds,
)

data class PasswordResetTokenRecord(
    val token: String,
    val userId: UserId,
    val email: Email,
    val expiresAtEpochSeconds: EpochSeconds,
)
