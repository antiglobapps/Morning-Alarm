package com.morningalarm.server.modules.auth.application.ports

import com.morningalarm.server.modules.auth.domain.AccessTokenPayload
import com.morningalarm.server.modules.auth.domain.AuthUser
import com.morningalarm.server.modules.auth.domain.PasswordResetTokenRecord
import com.morningalarm.server.modules.auth.domain.RefreshTokenRecord
import com.morningalarm.server.modules.auth.domain.SocialProvider
import com.morningalarm.server.modules.auth.domain.UserRole
import com.morningalarm.server.modules.user.domain.BusinessUser

interface AuthUserRepository {
    fun findByEmail(email: String): AuthUser?
    fun findBySocialAccount(provider: SocialProvider, externalSubject: String): AuthUser?
    fun findById(userId: String): AuthUser?
    fun countUsers(): Long
    fun upsertUser(user: AuthUser): AuthUser
    fun savePasswordResetToken(record: PasswordResetTokenRecord)
    fun consumePasswordResetToken(token: String): PasswordResetTokenRecord?
    fun saveRefreshToken(record: RefreshTokenRecord)
    fun consumeRefreshToken(token: String): RefreshTokenRecord?
    /** Invalidates all active refresh tokens for the given user (e.g. after a password reset). */
    fun revokeAllRefreshTokens(userId: String)
}

interface AuthEmailGateway {
    fun sendPasswordReset(email: String, token: String)
}

interface TokenFactory {
    fun newUserId(): String
    fun newRingtoneId(): String
    fun newRefreshToken(): String
    fun newPasswordResetToken(): String
    fun newTemporaryPassword(): String
    fun stableSubject(rawToken: String): String
}

interface PasswordHasher {
    fun hash(password: String): String
    fun matches(password: String, hash: String): Boolean
}

interface Clock {
    fun epochSeconds(): Long
}

interface AccessTokenService {
    fun issueToken(userId: String, role: UserRole, expiresAtEpochSeconds: Long): String
    fun verify(token: String): AccessTokenPayload?
}

interface BusinessUserRepository {
    fun findById(id: String): BusinessUser?
    fun findByEmail(email: String): BusinessUser?
    fun ensureUser(id: String, email: String?, displayName: String?, role: UserRole): BusinessUser
    fun updateRole(userId: String, role: UserRole)
}
