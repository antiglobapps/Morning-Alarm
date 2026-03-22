package com.morningalarm.server.modules.auth.domain

import com.morningalarm.EpochSeconds
import com.morningalarm.UserId

data class AccessTokenPayload(
    val userId: UserId,
    val role: UserRole,
    val expiresAtEpochSeconds: EpochSeconds,
)
