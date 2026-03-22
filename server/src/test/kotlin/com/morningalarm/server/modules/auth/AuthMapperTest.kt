package com.morningalarm.server.modules.auth

import com.morningalarm.dto.auth.SocialProviderDto
import com.morningalarm.dto.auth.UserRoleDto
import com.morningalarm.server.modules.auth.api.toDomain
import com.morningalarm.server.modules.auth.api.toDto
import com.morningalarm.server.modules.auth.domain.AuthSession
import com.morningalarm.server.modules.auth.domain.SocialProvider
import com.morningalarm.server.modules.auth.domain.UserRole
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthMapperTest {
    @Test
    fun `social provider dto maps to domain for all supported values`() {
        assertEquals(SocialProvider.GOOGLE, SocialProviderDto.GOOGLE.toDomain())
        assertEquals(SocialProvider.VK, SocialProviderDto.VK.toDomain())
        assertEquals(SocialProvider.FACEBOOK, SocialProviderDto.FACEBOOK.toDomain())
        assertEquals(SocialProvider.APPLE, SocialProviderDto.APPLE.toDomain())
    }

    @Test
    fun `auth session maps to dto preserving all fields and roles`() {
        val adminSession = AuthSession(
            userId = "admin-1",
            role = UserRole.ADMIN,
            bearerToken = "bearer-admin",
            refreshToken = "refresh-admin",
            expiresAtEpochSeconds = 100,
            isNewUser = true,
        )
        val userSession = AuthSession(
            userId = "user-1",
            role = UserRole.USER,
            bearerToken = "bearer-user",
            refreshToken = "refresh-user",
            expiresAtEpochSeconds = 200,
            isNewUser = false,
        )

        val adminDto = adminSession.toDto()
        val userDto = userSession.toDto()

        assertEquals("admin-1", adminDto.userId)
        assertEquals(UserRoleDto.ADMIN, adminDto.role)
        assertEquals("bearer-admin", adminDto.bearerToken)
        assertEquals("refresh-admin", adminDto.refreshToken)
        assertEquals(100, adminDto.expiresAtEpochSeconds)
        assertEquals(true, adminDto.isNewUser)

        assertEquals("user-1", userDto.userId)
        assertEquals(UserRoleDto.USER, userDto.role)
        assertEquals("bearer-user", userDto.bearerToken)
        assertEquals("refresh-user", userDto.refreshToken)
        assertEquals(200, userDto.expiresAtEpochSeconds)
        assertEquals(false, userDto.isNewUser)
    }
}
