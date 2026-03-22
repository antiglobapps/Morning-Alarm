package com.morningalarm.server.modules.auth.application

import com.morningalarm.server.modules.auth.application.ports.AuthEmailGateway
import com.morningalarm.server.modules.auth.application.ports.AuthUserRepository
import com.morningalarm.server.modules.auth.application.ports.PasswordHasher
import com.morningalarm.server.modules.auth.application.ports.TokenFactory
import com.morningalarm.server.modules.auth.domain.AuthSession
import com.morningalarm.server.modules.auth.domain.AuthUser
import com.morningalarm.server.modules.auth.domain.PasswordResetTokenRecord
import com.morningalarm.server.shared.audit.AuditEvent
import com.morningalarm.server.shared.audit.AuditLogger
import com.morningalarm.server.shared.errors.ConflictException
import com.morningalarm.server.shared.errors.UnauthorizedException
import com.morningalarm.server.shared.persistence.TransactionRunner

class EmailAuthService(
    private val authUserRepository: AuthUserRepository,
    private val authEmailGateway: AuthEmailGateway,
    private val tokenFactory: TokenFactory,
    private val passwordHasher: PasswordHasher,
    private val auditLogger: AuditLogger,
    private val transactionRunner: TransactionRunner,
    private val sessionManager: AuthSessionManager,
    private val passwordResetTokenTtlSeconds: Long,
) {
    fun registerWithEmail(
        email: String,
        password: String,
        displayName: String?,
    ): AuthSession {
        return transactionRunner.inTransaction {
            val normalizedEmail = sessionManager.normalizeEmail(email)
            sessionManager.validatePassword(password)
            if (authUserRepository.findByEmail(normalizedEmail) != null) {
                throw ConflictException("User with this email already exists")
            }

            val user = AuthUser(
                id = tokenFactory.newUserId(),
                email = normalizedEmail,
                displayName = displayName?.trim()?.takeIf { it.isNotEmpty() },
                passwordHash = passwordHasher.hash(password),
                socialAccounts = emptySet(),
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

    fun loginWithEmail(email: String, password: String): AuthSession {
        return transactionRunner.inTransaction {
            val normalizedEmail = sessionManager.normalizeEmail(email)
            val user = authUserRepository.findByEmail(normalizedEmail)
                ?: throw UnauthorizedException("Invalid email or password")
            val passwordHash = user.passwordHash
                ?: throw UnauthorizedException("Password login is not enabled for this user")

            if (!passwordHasher.matches(password, passwordHash)) {
                throw UnauthorizedException("Invalid email or password")
            }
            val businessUser = sessionManager.ensureBusinessUser(
                id = user.id,
                email = user.email,
                displayName = user.displayName,
            )
            sessionManager.issueSession(businessUser.id, businessUser.role, isNewUser = false)
        }
    }

    fun requestPasswordReset(email: String) {
        val normalizedEmail = sessionManager.normalizeEmail(email)
        val user = authUserRepository.findByEmail(normalizedEmail) ?: return
        val token = tokenFactory.newPasswordResetToken()
        authUserRepository.savePasswordResetToken(
            PasswordResetTokenRecord(
                token = token,
                userId = user.id,
                email = normalizedEmail,
                expiresAtEpochSeconds = sessionManager.epochSeconds() + passwordResetTokenTtlSeconds,
            ),
        )
        authEmailGateway.sendPasswordReset(normalizedEmail, token)
        auditLogger.log(AuditEvent.PasswordResetRequested(email = normalizedEmail))
    }

    fun confirmPasswordReset(token: String, newPassword: String): AuthSession {
        return transactionRunner.inTransaction {
            sessionManager.requireOpaqueToken(token)
            sessionManager.validatePassword(newPassword)
            val record = authUserRepository.consumePasswordResetToken(token)
                ?: throw UnauthorizedException("Invalid or expired password reset token")

            if (record.expiresAtEpochSeconds < sessionManager.epochSeconds()) {
                throw UnauthorizedException("Invalid or expired password reset token")
            }

            val user = authUserRepository.findById(record.userId)
                ?: throw UnauthorizedException("User for password reset was not found")

            authUserRepository.upsertUser(
                user.copy(
                    passwordHash = passwordHasher.hash(newPassword),
                ),
            )

            // Invalidate all existing sessions so stolen refresh tokens cannot be reused
            authUserRepository.revokeAllRefreshTokens(user.id)
            auditLogger.log(AuditEvent.SessionsRevoked(userId = user.id, reason = "password_reset"))

            val businessUser = sessionManager.ensureBusinessUser(
                id = user.id,
                email = user.email,
                displayName = user.displayName,
            )
            auditLogger.log(AuditEvent.PasswordResetConfirmed(userId = user.id))
            sessionManager.issueSession(businessUser.id, businessUser.role, isNewUser = false)
        }
    }

    fun refresh(refreshToken: String): AuthSession {
        return transactionRunner.inTransaction {
            sessionManager.requireOpaqueToken(refreshToken)
            val record = authUserRepository.consumeRefreshToken(refreshToken)
                ?: throw UnauthorizedException("Invalid or expired refresh token")

            if (record.expiresAtEpochSeconds < sessionManager.epochSeconds()) {
                throw UnauthorizedException("Invalid or expired refresh token")
            }

            val authUser = authUserRepository.findById(record.userId)
                ?: throw UnauthorizedException("User for refresh token was not found")
            val businessUser = sessionManager.ensureBusinessUser(
                id = authUser.id,
                email = authUser.email,
                displayName = authUser.displayName,
            )
            sessionManager.issueSession(businessUser.id, businessUser.role, isNewUser = false)
        }
    }
}
