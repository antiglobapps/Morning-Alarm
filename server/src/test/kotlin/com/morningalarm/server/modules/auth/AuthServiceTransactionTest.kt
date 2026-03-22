package com.morningalarm.server.modules.auth

import com.morningalarm.server.modules.auth.application.AuthService
import com.morningalarm.server.modules.auth.application.AuthSessionManager
import com.morningalarm.server.modules.auth.application.AdminLoginService
import com.morningalarm.server.modules.auth.application.EmailAuthService
import com.morningalarm.server.modules.auth.application.SocialAuthService
import com.morningalarm.server.modules.auth.application.ports.AccessTokenService
import com.morningalarm.server.modules.auth.application.ports.BusinessUserRepository
import com.morningalarm.server.modules.auth.domain.AccessTokenPayload
import com.morningalarm.server.modules.auth.domain.UserRole
import com.morningalarm.server.modules.auth.infra.AuthDatabaseSchema
import com.morningalarm.server.modules.auth.infra.InMemoryAuthEmailGateway
import com.morningalarm.server.modules.auth.infra.JavaClock
import com.morningalarm.server.modules.auth.infra.Pbkdf2PasswordHasher
import com.morningalarm.server.modules.auth.infra.PostgresAuthUserRepository
import com.morningalarm.server.modules.auth.infra.SecureTokenFactory
import com.morningalarm.server.modules.user.domain.BusinessUser
import com.morningalarm.server.modules.user.infra.UserDatabaseSchema
import com.morningalarm.server.shared.audit.NoOpAuditLogger
import com.morningalarm.server.shared.persistence.JdbcSessionManager
import com.morningalarm.server.shared.ratelimit.BruteForceProtector
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class AuthServiceTransactionTest {
    @Test
    fun `register with email rolls back auth user when business user persistence fails`() {
        val dataDir = Files.createTempDirectory("morning-alarm-auth-transaction").toString()
        createTestDataSource(dataDir).use { dataSource ->
            AuthDatabaseSchema(dataSource).ensureCreated()
            UserDatabaseSchema(dataSource).ensureCreated()

            val sessionManager = JdbcSessionManager(dataSource)
            val authUserRepository = PostgresAuthUserRepository(sessionManager)
            val authSessionManager = AuthSessionManager(
                authUserRepository = authUserRepository,
                businessUserRepository = FailingBusinessUserRepository(),
                tokenFactory = SecureTokenFactory(),
                accessTokenService = StubAccessTokenService(),
                clock = JavaClock(),
                adminEmails = emptySet(),
                accessTokenTtlSeconds = 3600,
                refreshTokenTtlSeconds = 7200,
            )
            val emailAuthService = EmailAuthService(
                authUserRepository = authUserRepository,
                authEmailGateway = InMemoryAuthEmailGateway(),
                tokenFactory = SecureTokenFactory(),
                passwordHasher = Pbkdf2PasswordHasher(),
                auditLogger = NoOpAuditLogger(),
                transactionRunner = sessionManager,
                sessionManager = authSessionManager,
                passwordResetTokenTtlSeconds = 600,
            )
            val service = AuthService(
                adminLoginService = AdminLoginService(
                    emailAuthService = emailAuthService,
                    auditLogger = NoOpAuditLogger(),
                    adminLoginBruteForce = BruteForceProtector(maxAttempts = 100, windowSeconds = 300),
                ),
                socialAuthService = SocialAuthService(
                    authUserRepository = authUserRepository,
                    tokenFactory = SecureTokenFactory(),
                    transactionRunner = sessionManager,
                    sessionManager = authSessionManager,
                ),
                emailAuthService = emailAuthService,
            )

            assertFailsWith<IllegalStateException> {
                service.registerWithEmail(
                    email = "rollback@example.com",
                    password = "very-secret",
                    displayName = "Rollback User",
                )
            }

            assertNull(authUserRepository.findByEmail("rollback@example.com"))
        }
    }
}

private class FailingBusinessUserRepository : BusinessUserRepository {
    override fun findById(id: String): BusinessUser? = null

    override fun findByEmail(email: String): BusinessUser? = null

    override fun ensureUser(id: String, email: String?, displayName: String?, role: UserRole): BusinessUser {
        throw IllegalStateException("Simulated business_users failure")
    }

    override fun updateRole(userId: String, role: UserRole) = Unit
}

private class StubAccessTokenService : AccessTokenService {
    override fun issueToken(userId: String, role: UserRole, expiresAtEpochSeconds: Long): String =
        "test-token-$userId"

    override fun verify(token: String): AccessTokenPayload? = null
}

private fun createTestDataSource(dataDir: String): HikariDataSource {
    val hikariConfig = HikariConfig().apply {
        jdbcUrl = "jdbc:h2:file:$dataDir/test-db;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
        username = "sa"
        password = ""
        driverClassName = "org.h2.Driver"
        maximumPoolSize = 2
        minimumIdle = 1
        isAutoCommit = true
        validate()
    }
    return HikariDataSource(hikariConfig)
}
