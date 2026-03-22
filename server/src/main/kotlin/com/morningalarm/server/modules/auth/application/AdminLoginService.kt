package com.morningalarm.server.modules.auth.application

import com.morningalarm.server.modules.auth.domain.AuthSession
import com.morningalarm.server.modules.auth.domain.UserRole
import com.morningalarm.server.shared.audit.AuditEvent
import com.morningalarm.server.shared.audit.AuditLogger
import com.morningalarm.server.shared.errors.ForbiddenException
import com.morningalarm.server.shared.errors.ValidationException
import com.morningalarm.server.shared.ratelimit.BruteForceProtector

class AdminLoginService(
    private val emailAuthService: EmailAuthService,
    private val auditLogger: AuditLogger,
    private val adminLoginBruteForce: BruteForceProtector? = null,
) {
    /**
     * Admin login: standard email/password check + admin access secret verification.
     * Returns session only if user has ADMIN role and the admin secret matches.
     * Applies brute-force rate limiting per email when [adminLoginBruteForce] is configured.
     */
    fun adminLogin(email: String, password: String, adminSecret: String, requiredAdminSecret: String?): AuthSession {
        val normalizedEmail = normalizeEmail(email)

        adminLoginBruteForce?.checkBlocked(normalizedEmail)

        if (requiredAdminSecret.isNullOrBlank()) {
            val reason = "Admin login not configured"
            auditLogger.log(AuditEvent.AdminLoginFailure(email = normalizedEmail, reason = reason))
            throw ForbiddenException("Admin login is not configured (SERVER_ADMIN_ACCESS_SECRET not set)")
        }
        if (adminSecret != requiredAdminSecret) {
            val reason = "Invalid admin secret"
            adminLoginBruteForce?.recordFailure(normalizedEmail)
            auditLogger.log(AuditEvent.AdminLoginFailure(email = normalizedEmail, reason = reason))
            throw ForbiddenException("Invalid admin access secret")
        }

        val session = try {
            emailAuthService.loginWithEmail(normalizedEmail, password)
        } catch (e: Exception) {
            adminLoginBruteForce?.recordFailure(normalizedEmail)
            auditLogger.log(AuditEvent.AdminLoginFailure(email = normalizedEmail, reason = e.message ?: "login failed"))
            throw e
        }

        if (session.role != UserRole.ADMIN) {
            adminLoginBruteForce?.recordFailure(normalizedEmail)
            auditLogger.log(AuditEvent.AdminLoginFailure(email = normalizedEmail, reason = "Not an admin"))
            throw ForbiddenException("Admin role is required")
        }

        adminLoginBruteForce?.recordSuccess(normalizedEmail)
        auditLogger.log(AuditEvent.AdminLoginSuccess(adminId = session.userId, email = normalizedEmail))
        return session
    }

    private fun normalizeEmail(email: String): String {
        val normalized = email.trim().lowercase()
        if (normalized.isBlank() || !normalized.contains("@")) {
            throw ValidationException("Email must be valid")
        }
        return normalized
    }
}
