package com.morningalarm.server.modules.auth.application

import com.morningalarm.server.modules.auth.application.ports.AuthEmailGateway
import com.morningalarm.server.modules.auth.application.ports.AuthUserRepository
import com.morningalarm.server.modules.auth.application.ports.BusinessUserRepository
import com.morningalarm.server.modules.auth.application.ports.Clock
import com.morningalarm.server.modules.auth.application.ports.PasswordHasher
import com.morningalarm.server.modules.auth.application.ports.TokenFactory
import com.morningalarm.server.modules.auth.application.ports.AccessTokenService
import com.morningalarm.server.modules.auth.domain.AuthUser
import com.morningalarm.server.modules.auth.domain.AuthSession
import com.morningalarm.server.modules.auth.domain.PasswordResetTokenRecord
import com.morningalarm.server.modules.auth.domain.RefreshTokenRecord
import com.morningalarm.server.modules.auth.domain.SocialAccount
import com.morningalarm.server.modules.auth.domain.SocialProvider
import com.morningalarm.server.modules.auth.domain.UserRole
import com.morningalarm.server.shared.errors.ConflictException
import com.morningalarm.server.shared.errors.ForbiddenException
import com.morningalarm.server.shared.errors.UnauthorizedException
import com.morningalarm.server.shared.errors.ValidationException

data class AdminBootstrapResult(
    val userId: String,
    val email: String,
    val temporaryPassword: String,
)

class AuthService(
    private val authUserRepository: AuthUserRepository,
    private val businessUserRepository: BusinessUserRepository,
    private val authEmailGateway: AuthEmailGateway,
    private val tokenFactory: TokenFactory,
    private val accessTokenService: AccessTokenService,
    private val passwordHasher: PasswordHasher,
    private val clock: Clock,
    private val adminEmails: Set<String>,
    private val accessTokenTtlSeconds: Long,
    private val refreshTokenTtlSeconds: Long,
    private val passwordResetTokenTtlSeconds: Long,
) {
    /**
     * Creates the first admin account directly on the server side.
     * Returns a temporary password that must be rotated by the operator afterwards.
     */
    fun createAdmin(email: String, displayName: String? = null): AdminBootstrapResult {
        val normalizedEmail = normalizeEmail(email)
        if (authUserRepository.findByEmail(normalizedEmail) != null) {
            throw ConflictException("User with this email already exists")
        }

        val temporaryPassword = "adm_${tokenFactory.newPasswordResetToken().removePrefix("rst_").take(16)}"
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

        return AdminBootstrapResult(
            userId = user.id,
            email = normalizedEmail,
            temporaryPassword = temporaryPassword,
        )
    }

    /**
     * Promotes an existing user to ADMIN role.
     * Called only from server-side bootstrap command — never from HTTP.
     */
    fun promoteToAdmin(email: String): String {
        val normalizedEmail = email.trim().lowercase()
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
        return authUser.id
    }

    /**
     * Admin login: standard email/password check + admin access secret verification.
     * Returns session only if user has ADMIN role and the admin secret matches.
     */
    fun adminLogin(email: String, password: String, adminSecret: String, requiredAdminSecret: String?): AuthSession {
        if (requiredAdminSecret.isNullOrBlank()) {
            throw ForbiddenException("Admin login is not configured (SERVER_ADMIN_ACCESS_SECRET not set)")
        }
        if (adminSecret != requiredAdminSecret) {
            throw ForbiddenException("Invalid admin access secret")
        }
        val session = loginWithEmail(email, password)
        if (session.role != UserRole.ADMIN) {
            throw ForbiddenException("Admin role is required")
        }
        return session
    }

    fun socialAuth(
        provider: SocialProvider,
        token: String,
        email: String?,
        displayName: String?,
    ): AuthSession {
        requireOpaqueToken(token)
        val subject = tokenFactory.stableSubject(token)
        val existing = authUserRepository.findBySocialAccount(provider, subject)
        if (existing != null) {
            val businessUser = businessUserRepository.ensureUser(
                id = existing.id,
                email = existing.email,
                displayName = existing.displayName,
                role = roleForEmail(existing.email),
            )
            return issueSession(businessUser.id, businessUser.role, isNewUser = false)
        }

        val normalizedEmail = email?.trim()?.lowercase()
        val attachedUser = normalizedEmail?.let(authUserRepository::findByEmail)
        if (attachedUser != null) {
            val merged = attachedUser.copy(
                socialAccounts = attachedUser.socialAccounts + SocialAccount(provider, subject),
            )
            authUserRepository.upsertUser(merged)
            val businessUser = businessUserRepository.ensureUser(
                id = merged.id,
                email = merged.email,
                displayName = merged.displayName,
                role = roleForEmail(merged.email),
            )
            return issueSession(businessUser.id, businessUser.role, isNewUser = false)
        }

        val user = AuthUser(
            id = tokenFactory.newUserId(),
            email = normalizedEmail,
            displayName = displayName?.trim()?.takeIf { it.isNotEmpty() },
            passwordHash = null,
            socialAccounts = setOf(SocialAccount(provider, subject)),
        )
        authUserRepository.upsertUser(user)
        val businessUser = businessUserRepository.ensureUser(
            id = user.id,
            email = user.email,
            displayName = user.displayName,
            role = roleForEmail(user.email),
        )
        return issueSession(businessUser.id, businessUser.role, isNewUser = true)
    }

    fun registerWithEmail(
        email: String,
        password: String,
        displayName: String?,
    ): AuthSession {
        val normalizedEmail = normalizeEmail(email)
        validatePassword(password)
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
        val businessUser = businessUserRepository.ensureUser(
            id = user.id,
            email = user.email,
            displayName = user.displayName,
            role = roleForEmail(user.email),
        )
        return issueSession(businessUser.id, businessUser.role, isNewUser = true)
    }

    fun loginWithEmail(email: String, password: String): AuthSession {
        val normalizedEmail = normalizeEmail(email)
        val user = authUserRepository.findByEmail(normalizedEmail)
            ?: throw UnauthorizedException("Invalid email or password")
        val passwordHash = user.passwordHash
            ?: throw UnauthorizedException("Password login is not enabled for this user")

        if (!passwordHasher.matches(password, passwordHash)) {
            throw UnauthorizedException("Invalid email or password")
        }
        val businessUser = businessUserRepository.ensureUser(
            id = user.id,
            email = user.email,
            displayName = user.displayName,
            role = roleForEmail(user.email),
        )
        return issueSession(businessUser.id, businessUser.role, isNewUser = false)
    }

    fun requestPasswordReset(email: String) {
        val normalizedEmail = normalizeEmail(email)
        val user = authUserRepository.findByEmail(normalizedEmail) ?: return
        val token = tokenFactory.newPasswordResetToken()
        authUserRepository.savePasswordResetToken(
            PasswordResetTokenRecord(
                token = token,
                userId = user.id,
                email = normalizedEmail,
                expiresAtEpochSeconds = clock.epochSeconds() + passwordResetTokenTtlSeconds,
            ),
        )
        authEmailGateway.sendPasswordReset(normalizedEmail, token)
    }

    fun confirmPasswordReset(token: String, newPassword: String): AuthSession {
        requireOpaqueToken(token)
        validatePassword(newPassword)
        val record = authUserRepository.consumePasswordResetToken(token)
            ?: throw UnauthorizedException("Invalid or expired password reset token")

        if (record.expiresAtEpochSeconds < clock.epochSeconds()) {
            throw UnauthorizedException("Invalid or expired password reset token")
        }

        val user = authUserRepository.findById(record.userId)
            ?: throw UnauthorizedException("User for password reset was not found")

        authUserRepository.upsertUser(
            user.copy(
                passwordHash = passwordHasher.hash(newPassword),
            ),
        )
        val businessUser = businessUserRepository.ensureUser(
            id = user.id,
            email = user.email,
            displayName = user.displayName,
            role = roleForEmail(user.email),
        )
        return issueSession(businessUser.id, businessUser.role, isNewUser = false)
    }

    fun refresh(refreshToken: String): AuthSession {
        requireOpaqueToken(refreshToken)
        val record = authUserRepository.consumeRefreshToken(refreshToken)
            ?: throw UnauthorizedException("Invalid or expired refresh token")

        if (record.expiresAtEpochSeconds < clock.epochSeconds()) {
            throw UnauthorizedException("Invalid or expired refresh token")
        }

        val authUser = authUserRepository.findById(record.userId)
            ?: throw UnauthorizedException("User for refresh token was not found")
        val businessUser = businessUserRepository.ensureUser(
            id = authUser.id,
            email = authUser.email,
            displayName = authUser.displayName,
            role = roleForEmail(authUser.email),
        )
        return issueSession(businessUser.id, businessUser.role, isNewUser = false)
    }

    private fun issueSession(userId: String, role: UserRole, isNewUser: Boolean): AuthSession {
        val now = clock.epochSeconds()
        val expiresAt = now + accessTokenTtlSeconds
        val refreshToken = tokenFactory.newRefreshToken()
        authUserRepository.saveRefreshToken(
            RefreshTokenRecord(
                token = refreshToken,
                userId = userId,
                expiresAtEpochSeconds = now + refreshTokenTtlSeconds,
            ),
        )
        return AuthSession(
            userId = userId,
            role = role,
            bearerToken = accessTokenService.issueToken(userId, role, expiresAt),
            refreshToken = refreshToken,
            expiresAtEpochSeconds = expiresAt,
            isNewUser = isNewUser,
        )
    }

    private fun normalizeEmail(email: String): String {
        val normalized = email.trim().lowercase()
        if (normalized.isBlank() || !normalized.contains("@")) {
            throw ValidationException("Email must be valid")
        }
        return normalized
    }

    private fun validatePassword(password: String) {
        if (password.length < 8) {
            throw ValidationException("Password must contain at least 8 characters")
        }
    }

    private fun requireOpaqueToken(token: String) {
        if (token.isBlank()) {
            throw ValidationException("Token must not be blank")
        }
    }

    private fun roleForEmail(email: String?): UserRole {
        return if (email != null && adminEmails.contains(email.trim().lowercase())) {
            UserRole.ADMIN
        } else {
            UserRole.USER
        }
    }
}
