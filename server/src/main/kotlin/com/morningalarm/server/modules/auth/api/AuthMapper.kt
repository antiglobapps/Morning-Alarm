package com.morningalarm.server.modules.auth.api

import com.morningalarm.dto.auth.AuthSessionDto
import com.morningalarm.dto.auth.SocialProviderDto
import com.morningalarm.dto.auth.UserRoleDto
import com.morningalarm.server.modules.auth.domain.AuthSession
import com.morningalarm.server.modules.auth.domain.SocialProvider
import com.morningalarm.server.modules.auth.domain.UserRole

fun SocialProviderDto.toDomain(): SocialProvider = when (this) {
    SocialProviderDto.GOOGLE -> SocialProvider.GOOGLE
    SocialProviderDto.VK -> SocialProvider.VK
    SocialProviderDto.FACEBOOK -> SocialProvider.FACEBOOK
    SocialProviderDto.APPLE -> SocialProvider.APPLE
}

fun AuthSession.toDto(): AuthSessionDto = AuthSessionDto(
    userId = userId,
    role = role.toDto(),
    bearerToken = bearerToken,
    refreshToken = refreshToken,
    expiresAtEpochSeconds = expiresAtEpochSeconds,
    isNewUser = isNewUser,
)

private fun UserRole.toDto(): UserRoleDto = when (this) {
    UserRole.ADMIN -> UserRoleDto.ADMIN
    UserRole.USER -> UserRoleDto.USER
}
