package com.morningalarm.dto.auth

import kotlinx.serialization.Serializable

@Serializable
enum class SocialProviderDto {
    GOOGLE,
    VK,
    FACEBOOK,
    APPLE,
}

@Serializable
enum class UserRoleDto {
    ADMIN,
    USER,
}

@Serializable
data class AuthSessionDto(
    val userId: String,
    val role: UserRoleDto,
    val bearerToken: String,
    val refreshToken: String,
    val expiresAtEpochSeconds: Long,
    val isNewUser: Boolean,
)

@Serializable
data class SocialAuthRequestDto(
    val provider: SocialProviderDto,
    val token: String,
    val email: String? = null,
    val displayName: String? = null,
)

@Serializable
data class SocialAuthResponseDto(
    val session: AuthSessionDto,
)

@Serializable
data class EmailRegisterRequestDto(
    val email: String,
    val password: String,
    val displayName: String? = null,
)

@Serializable
data class EmailRegisterResponseDto(
    val session: AuthSessionDto,
)

@Serializable
data class EmailLoginRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class EmailLoginResponseDto(
    val session: AuthSessionDto,
)

@Serializable
data class PasswordResetRequestDto(
    val email: String,
)

@Serializable
data class PasswordResetRequestResponseDto(
    val email: String,
    val resetRequested: Boolean,
)

@Serializable
data class PasswordResetConfirmRequestDto(
    val token: String,
    val newPassword: String,
)

@Serializable
data class PasswordResetConfirmResponseDto(
    val session: AuthSessionDto,
)

@Serializable
data class RefreshTokenRequestDto(
    val refreshToken: String,
)

@Serializable
data class RefreshTokenResponseDto(
    val session: AuthSessionDto,
)

@Serializable
data class AdminLoginRequestDto(
    val email: String,
    val password: String,
    val adminSecret: String,
)

@Serializable
data class AdminLoginResponseDto(
    val session: AuthSessionDto,
)
