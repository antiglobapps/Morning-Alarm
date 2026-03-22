package com.morningalarm.server.modules.auth.application

import com.morningalarm.server.modules.auth.application.ports.AuthUserRepository
import com.morningalarm.server.modules.auth.application.ports.TokenFactory
import com.morningalarm.server.modules.auth.domain.AuthSession
import com.morningalarm.server.modules.auth.domain.AuthUser
import com.morningalarm.server.modules.auth.domain.SocialAccount
import com.morningalarm.server.modules.auth.domain.SocialProvider
import com.morningalarm.server.shared.errors.ValidationException
import com.morningalarm.server.shared.persistence.TransactionRunner

class SocialAuthService(
    private val authUserRepository: AuthUserRepository,
    private val tokenFactory: TokenFactory,
    private val transactionRunner: TransactionRunner,
    private val sessionManager: AuthSessionManager,
) {
    fun socialAuth(
        provider: SocialProvider,
        token: String,
        email: String?,
        displayName: String?,
    ): AuthSession {
        return transactionRunner.inTransaction {
            requireOpaqueToken(token)
            val subject = tokenFactory.stableSubject(token)
            val existing = authUserRepository.findBySocialAccount(provider, subject)
            if (existing != null) {
                val businessUser = sessionManager.ensureBusinessUser(
                    id = existing.id,
                    email = existing.email,
                    displayName = existing.displayName,
                )
                return@inTransaction sessionManager.issueSession(businessUser.id, businessUser.role, isNewUser = false)
            }

            val normalizedEmail = email?.trim()?.lowercase()
            val attachedUser = normalizedEmail?.let(authUserRepository::findByEmail)
            if (attachedUser != null) {
                val merged = attachedUser.copy(
                    socialAccounts = attachedUser.socialAccounts + SocialAccount(provider, subject),
                )
                authUserRepository.upsertUser(merged)
                val businessUser = sessionManager.ensureBusinessUser(
                    id = merged.id,
                    email = merged.email,
                    displayName = merged.displayName,
                )
                return@inTransaction sessionManager.issueSession(businessUser.id, businessUser.role, isNewUser = false)
            }

            val user = AuthUser(
                id = tokenFactory.newUserId(),
                email = normalizedEmail,
                displayName = displayName?.trim()?.takeIf { it.isNotEmpty() },
                passwordHash = null,
                socialAccounts = setOf(SocialAccount(provider, subject)),
            )
            authUserRepository.upsertUser(user)
            val businessUser = sessionManager.ensureBusinessUser(
                id = user.id,
                email = user.email,
                displayName = user.displayName,
            )
            sessionManager.issueSession(businessUser.id, businessUser.role, isNewUser = true)
        }
    }

    private fun requireOpaqueToken(token: String) {
        if (token.isBlank()) {
            throw ValidationException("Token must not be blank")
        }
    }
}
