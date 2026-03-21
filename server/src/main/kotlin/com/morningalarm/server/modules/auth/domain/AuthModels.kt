package com.morningalarm.server.modules.auth.domain

data class AuthUser(
    val id: String,
    val email: String?,
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
    val userId: String,
    val role: UserRole,
    val bearerToken: String,
    val refreshToken: String,
    val expiresAtEpochSeconds: Long,
    val isNewUser: Boolean,
)

enum class UserRole {
    ADMIN,
    USER,
}

data class RefreshTokenRecord(
    val token: String,
    val userId: String,
    val expiresAtEpochSeconds: Long,
)

data class PasswordResetTokenRecord(
    val token: String,
    val userId: String,
    val email: String,
    val expiresAtEpochSeconds: Long,
)
