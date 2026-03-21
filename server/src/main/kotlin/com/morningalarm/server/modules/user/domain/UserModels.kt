package com.morningalarm.server.modules.user.domain

import com.morningalarm.server.modules.auth.domain.UserRole

data class BusinessUser(
    val id: String,
    val email: String?,
    val displayName: String?,
    val role: UserRole,
)
