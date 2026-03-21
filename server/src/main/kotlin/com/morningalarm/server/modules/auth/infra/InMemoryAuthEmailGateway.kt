package com.morningalarm.server.modules.auth.infra

import com.morningalarm.server.modules.auth.application.ports.AuthEmailGateway
import java.util.concurrent.ConcurrentHashMap

class InMemoryAuthEmailGateway : AuthEmailGateway {
    private val lastPasswordResetTokenByEmail = ConcurrentHashMap<String, String>()

    override fun sendPasswordReset(email: String, token: String) {
        lastPasswordResetTokenByEmail[email] = token
    }

    fun lastResetToken(email: String): String? = lastPasswordResetTokenByEmail[email]
}
