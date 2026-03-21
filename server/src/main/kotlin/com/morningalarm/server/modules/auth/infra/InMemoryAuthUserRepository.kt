package com.morningalarm.server.modules.auth.infra

import com.morningalarm.server.modules.auth.application.ports.AuthUserRepository
import com.morningalarm.server.modules.auth.domain.AuthUser
import com.morningalarm.server.modules.auth.domain.PasswordResetTokenRecord
import com.morningalarm.server.modules.auth.domain.RefreshTokenRecord
import com.morningalarm.server.modules.auth.domain.SocialProvider
import java.util.concurrent.ConcurrentHashMap

class InMemoryAuthUserRepository : AuthUserRepository {
    private val usersById = ConcurrentHashMap<String, AuthUser>()
    private val userIdByEmail = ConcurrentHashMap<String, String>()
    private val userIdBySocial = ConcurrentHashMap<String, String>()
    private val passwordResetTokens = ConcurrentHashMap<String, PasswordResetTokenRecord>()
    private val refreshTokens = ConcurrentHashMap<String, RefreshTokenRecord>()

    override fun findByEmail(email: String): AuthUser? {
        val userId = userIdByEmail[email] ?: return null
        return usersById[userId]
    }

    override fun findBySocialAccount(provider: SocialProvider, externalSubject: String): AuthUser? {
        val userId = userIdBySocial[socialKey(provider, externalSubject)] ?: return null
        return usersById[userId]
    }

    override fun findById(userId: String): AuthUser? = usersById[userId]

    override fun upsertUser(user: AuthUser): AuthUser {
        usersById[user.id] = user
        user.email?.let { userIdByEmail[it] = user.id }
        user.socialAccounts.forEach { account ->
            userIdBySocial[socialKey(account.provider, account.externalSubject)] = user.id
        }
        return user
    }

    override fun savePasswordResetToken(record: PasswordResetTokenRecord) {
        passwordResetTokens[record.token] = record
    }

    override fun consumePasswordResetToken(token: String): PasswordResetTokenRecord? {
        return passwordResetTokens.remove(token)
    }

    override fun saveRefreshToken(record: RefreshTokenRecord) {
        refreshTokens[record.token] = record
    }

    override fun consumeRefreshToken(token: String): RefreshTokenRecord? {
        return refreshTokens.remove(token)
    }

    private fun socialKey(provider: SocialProvider, externalSubject: String): String {
        return "${provider.name}:$externalSubject"
    }
}
