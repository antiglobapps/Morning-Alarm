package com.morningalarm.server.modules.auth

import com.morningalarm.server.modules.auth.application.AdminLoginService
import com.morningalarm.server.modules.auth.application.AuthSessionManager
import com.morningalarm.server.modules.auth.application.EmailAuthService
import com.morningalarm.server.modules.auth.application.SocialAuthService
import com.morningalarm.server.modules.auth.application.ports.AccessTokenService
import com.morningalarm.server.modules.auth.domain.AccessTokenPayload
import com.morningalarm.server.modules.auth.domain.SocialProvider
import com.morningalarm.server.modules.auth.domain.UserRole
import com.morningalarm.server.modules.auth.infra.AuthDatabaseSchema
import com.morningalarm.server.modules.auth.infra.InMemoryAuthEmailGateway
import com.morningalarm.server.modules.auth.infra.JavaClock
import com.morningalarm.server.modules.auth.infra.Pbkdf2PasswordHasher
import com.morningalarm.server.modules.auth.infra.PostgresAuthUserRepository
import com.morningalarm.server.modules.auth.infra.SecureTokenFactory
import com.morningalarm.server.modules.user.infra.PostgresBusinessUserRepository
import com.morningalarm.server.modules.user.infra.UserDatabaseSchema
import com.morningalarm.server.shared.audit.NoOpAuditLogger
import com.morningalarm.server.shared.persistence.JdbcSessionManager
import com.morningalarm.server.shared.ratelimit.BruteForceProtector
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AuthScenarioServicesTest {
    @Test
    fun `email auth service registers logs in and refreshes a user`() {
        createAuthScenario().use { scenario ->
            val session = scenario.emailAuthService.registerWithEmail(
                email = "user@example.com",
                password = "very-secret",
                displayName = "User",
            )
            assertEquals(UserRole.USER, session.role)
            assertTrue(session.isNewUser)

            val login = scenario.emailAuthService.loginWithEmail(
                email = "user@example.com",
                password = "very-secret",
            )
            assertFalse(login.isNewUser)
            assertEquals(session.userId, login.userId)

            val refresh = scenario.emailAuthService.refresh(login.refreshToken)
            assertEquals(login.userId, refresh.userId)
            assertEquals(login.role, refresh.role)
            assertTrue(refresh.refreshToken.isNotBlank())
        }
    }

    @Test
    fun `social auth service creates a user and reuses existing social account`() {
        createAuthScenario().use { scenario ->
            val first = scenario.socialAuthService.socialAuth(
                provider = SocialProvider.GOOGLE,
                token = "google-token-1",
                email = "social@example.com",
                displayName = "Social User",
            )
            assertTrue(first.isNewUser)

            val second = scenario.socialAuthService.socialAuth(
                provider = SocialProvider.GOOGLE,
                token = "google-token-1",
                email = null,
                displayName = null,
            )
            assertFalse(second.isNewUser)
            assertEquals(first.userId, second.userId)
        }
    }

    @Test
    fun `admin login service enforces admin secret and admin role`() {
        createAuthScenario(adminEmails = setOf("admin@example.com")).use { scenario ->
            scenario.emailAuthService.registerWithEmail(
                email = "admin@example.com",
                password = "very-secret",
                displayName = "Admin",
            )
            scenario.emailAuthService.registerWithEmail(
                email = "user@example.com",
                password = "very-secret",
                displayName = "User",
            )

            assertFailsWith<com.morningalarm.server.shared.errors.ForbiddenException> {
                scenario.adminLoginService.adminLogin(
                    email = "admin@example.com",
                    password = "very-secret",
                    adminSecret = "wrong-secret",
                    requiredAdminSecret = "top-secret",
                )
            }

            val success = scenario.adminLoginService.adminLogin(
                email = "admin@example.com",
                password = "very-secret",
                adminSecret = "top-secret",
                requiredAdminSecret = "top-secret",
            )
            assertEquals(UserRole.ADMIN, success.role)

            assertFailsWith<com.morningalarm.server.shared.errors.ForbiddenException> {
                scenario.adminLoginService.adminLogin(
                    email = "user@example.com",
                    password = "very-secret",
                    adminSecret = "top-secret",
                    requiredAdminSecret = "top-secret",
                )
            }
        }
    }
}

private fun createAuthScenario(adminEmails: Set<String> = emptySet()): AuthScenarioContext {
    val dataSource = createTestDataSource()
    AuthDatabaseSchema(dataSource).ensureCreated()
    UserDatabaseSchema(dataSource).ensureCreated()

    val sessionManager = JdbcSessionManager(dataSource)
    val authUserRepository = PostgresAuthUserRepository(sessionManager)
    val businessUserRepository = PostgresBusinessUserRepository(sessionManager)
    val tokenFactory = SecureTokenFactory()
    val accessTokenService = ScenarioStubAccessTokenService()
    val authSessionManager = AuthSessionManager(
        authUserRepository = authUserRepository,
        businessUserRepository = businessUserRepository,
        tokenFactory = tokenFactory,
        accessTokenService = accessTokenService,
        clock = JavaClock(),
        adminEmails = adminEmails,
        accessTokenTtlSeconds = 3600,
        refreshTokenTtlSeconds = 7200,
    )
    val emailAuthService = EmailAuthService(
        authUserRepository = authUserRepository,
        authEmailGateway = InMemoryAuthEmailGateway(),
        tokenFactory = tokenFactory,
        passwordHasher = Pbkdf2PasswordHasher(),
        auditLogger = NoOpAuditLogger(),
        transactionRunner = sessionManager,
        sessionManager = authSessionManager,
        passwordResetTokenTtlSeconds = 600,
    )
    return AuthScenarioContext(
        dataSource = dataSource,
        emailAuthService = emailAuthService,
        socialAuthService = SocialAuthService(
            authUserRepository = authUserRepository,
            tokenFactory = tokenFactory,
            transactionRunner = sessionManager,
            sessionManager = authSessionManager,
        ),
        adminLoginService = AdminLoginService(
            emailAuthService = emailAuthService,
            auditLogger = NoOpAuditLogger(),
            adminLoginBruteForce = BruteForceProtector(maxAttempts = 100, windowSeconds = 300),
        ),
    )
}

private data class AuthScenarioContext(
    val dataSource: HikariDataSource,
    val emailAuthService: EmailAuthService,
    val socialAuthService: SocialAuthService,
    val adminLoginService: AdminLoginService,
) : AutoCloseable {
    override fun close() {
        dataSource.close()
    }
}

private class ScenarioStubAccessTokenService : AccessTokenService {
    override fun issueToken(userId: String, role: UserRole, expiresAtEpochSeconds: Long): String =
        "test-token-$userId"

    override fun verify(token: String): AccessTokenPayload? = null
}

private fun createTestDataSource(): HikariDataSource {
    val dataDir = Files.createTempDirectory("morning-alarm-auth-scenarios").toString()
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
