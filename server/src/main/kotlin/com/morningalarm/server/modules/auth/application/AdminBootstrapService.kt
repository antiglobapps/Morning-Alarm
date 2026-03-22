package com.morningalarm.server.modules.auth.application

import com.morningalarm.server.modules.auth.application.ports.AuthUserRepository
import com.morningalarm.server.modules.auth.application.ports.BusinessUserRepository
import com.morningalarm.server.modules.auth.application.ports.PasswordHasher
import com.morningalarm.server.modules.auth.application.ports.TokenFactory
import com.morningalarm.server.modules.auth.domain.AuthUser
import com.morningalarm.server.modules.auth.domain.UserRole
import com.morningalarm.server.shared.audit.AuditEvent
import com.morningalarm.server.shared.audit.AuditLogger
import com.morningalarm.server.shared.errors.ConflictException
import com.morningalarm.server.shared.errors.ValidationException
import com.morningalarm.server.shared.persistence.TransactionRunner

data class AdminBootstrapResult(
    val userId: String,
    val email: String,
    val temporaryPassword: String,
)

data class DevAdminBootstrapResult(
    val userId: String,
    val email: String,
    val password: String,
)

class AdminBootstrapService(
    private val authUserRepository: AuthUserRepository,
    private val businessUserRepository: BusinessUserRepository,
    private val tokenFactory: TokenFactory,
    private val passwordHasher: PasswordHasher,
    private val auditLogger: AuditLogger,
    private val transactionRunner: TransactionRunner,
) {
    /**
     * Creates the first admin account directly on the server side.
     * Returns a temporary password that must be rotated by the operator afterwards.
     */
    fun createAdmin(email: String, displayName: String? = null): AdminBootstrapResult {
        return transactionRunner.inTransaction {
            val normalizedEmail = normalizeEmail(email)
            if (authUserRepository.findByEmail(normalizedEmail) != null) {
                throw ConflictException("User with this email already exists")
            }

            val temporaryPassword = tokenFactory.newTemporaryPassword()
            val user = AuthUser(
                id = tokenFactory.newUserId(),
                email = normalizedEmail,
                displayName = displayName?.trim()?.takeIf { it.isNotEmpty() },
                passwordHash = passwordHasher.hash(temporaryPassword),
                socialAccounts = emptySet(),
            )
            authUserRepository.upsertUser(user)
            businessUserRepository.ensureUser(
                id = user.id,
                email = user.email,
                displayName = user.displayName,
                role = UserRole.ADMIN,
            )

            auditLogger.log(AuditEvent.AdminCreated(adminId = user.id, email = normalizedEmail))

            AdminBootstrapResult(
                userId = user.id,
                email = normalizedEmail,
                temporaryPassword = temporaryPassword,
            )
        }
    }

    /**
     * Creates a predictable local admin only for empty dev databases.
     * The password is stable on purpose so local clients can prefill it.
     */
    fun createDevAdminIfDatabaseEmpty(
        email: String,
        password: String,
        displayName: String? = null,
    ): DevAdminBootstrapResult? {
        return transactionRunner.inTransaction {
            val normalizedEmail = normalizeEmail(email)
            validatePassword(password)

            if (authUserRepository.countUsers() > 0) {
                return@inTransaction null
            }

            val user = AuthUser(
                id = tokenFactory.newUserId(),
                email = normalizedEmail,
                displayName = displayName?.trim()?.takeIf { it.isNotEmpty() },
                passwordHash = passwordHasher.hash(password),
                socialAccounts = emptySet(),
            )
            authUserRepository.upsertUser(user)
            businessUserRepository.ensureUser(
                id = user.id,
                email = user.email,
                displayName = user.displayName,
                role = UserRole.ADMIN,
            )

            auditLogger.log(AuditEvent.AdminCreated(adminId = user.id, email = normalizedEmail))

            DevAdminBootstrapResult(
                userId = user.id,
                email = normalizedEmail,
                password = password,
            )
        }
    }

    /**
     * Promotes an existing user to ADMIN role.
     * Called only from server-side bootstrap command — never from HTTP.
     */
    fun promoteToAdmin(email: String): String {
        return transactionRunner.inTransaction {
            val normalizedEmail = normalizeEmail(email)
            val authUser = authUserRepository.findByEmail(normalizedEmail)
                ?: throw ValidationException("No user found with email: $normalizedEmail")
            val businessUser = businessUserRepository.ensureUser(
                id = authUser.id,
                email = authUser.email,
                displayName = authUser.displayName,
                role = UserRole.ADMIN,
            )
            if (businessUser.role != UserRole.ADMIN) {
                businessUserRepository.updateRole(authUser.id, UserRole.ADMIN)
            }

            auditLogger.log(AuditEvent.AdminPromoted(adminId = authUser.id, email = normalizedEmail))
            authUser.id
        }
    }

    private fun normalizeEmail(value: String): String {
        val normalized = value.trim().lowercase()
        if (normalized.isBlank()) {
            throw ValidationException("Email must not be blank")
        }
        if (!normalized.contains('@')) {
            throw ValidationException("Email must contain @")
        }
        return normalized
    }

    private fun validatePassword(password: String) {
        if (password.length < 8) {
            throw ValidationException("Password must be at least 8 characters")
        }
    }
}
