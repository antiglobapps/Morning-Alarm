package com.morningalarm.server.modules.auth.domain

data class AccessTokenPayload(
    val userId: String,
    val role: UserRole,
    val expiresAtEpochSeconds: Long,
)
