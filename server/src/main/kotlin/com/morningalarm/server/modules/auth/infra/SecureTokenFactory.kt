package com.morningalarm.server.modules.auth.infra

import com.morningalarm.server.modules.auth.application.ports.TokenFactory
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID

class SecureTokenFactory : TokenFactory {
    override fun newUserId(): String = "usr_${UUID.randomUUID()}"

    override fun newRingtoneId(): String = "rng_${UUID.randomUUID()}"

    override fun newRefreshToken(): String = "rf_${randomToken()}"

    override fun newPasswordResetToken(): String = "rst_${randomToken()}"

    override fun stableSubject(rawToken: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(rawToken.toByteArray())
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }

    private fun randomToken(): String {
        val bytes = ByteArray(32).also(secureRandom::nextBytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private companion object {
        val secureRandom = SecureRandom()
    }
}
