package com.morningalarm.server.modules.user.domain

import com.morningalarm.Email
import com.morningalarm.UserId
import com.morningalarm.server.modules.auth.domain.UserRole

data class BusinessUser(
    val id: UserId,
    val email: Email?,
    val displayName: String?,
    val role: UserRole,
)
