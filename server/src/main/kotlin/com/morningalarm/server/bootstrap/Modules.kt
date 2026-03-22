package com.morningalarm.server.bootstrap

import com.morningalarm.server.modules.auth.application.AdminBootstrapService
import com.morningalarm.server.modules.auth.application.AdminLoginService
import com.morningalarm.server.modules.auth.application.AuthService
import com.morningalarm.server.modules.auth.application.AuthSessionManager
import com.morningalarm.server.modules.auth.application.EmailAuthService
import com.morningalarm.server.modules.auth.application.SocialAuthService
import com.morningalarm.server.modules.auth.application.ports.AccessTokenService
import com.morningalarm.server.modules.auth.application.ports.AuthEmailGateway
import com.morningalarm.server.modules.auth.application.ports.AuthUserRepository
import com.morningalarm.server.modules.auth.application.ports.BusinessUserRepository
import com.morningalarm.server.modules.auth.application.ports.Clock
import com.morningalarm.server.modules.auth.application.ports.PasswordHasher
import com.morningalarm.server.modules.auth.application.ports.TokenFactory
import com.morningalarm.server.modules.auth.infra.AuthDatabaseSchema
import com.morningalarm.server.modules.auth.infra.InMemoryAuthEmailGateway
import com.morningalarm.server.modules.auth.infra.PostgresAuthUserRepository
import com.morningalarm.server.modules.auth.infra.JavaClock
import com.morningalarm.server.modules.auth.infra.Pbkdf2PasswordHasher
import com.morningalarm.server.modules.auth.infra.SecureTokenFactory
import com.morningalarm.server.modules.media.application.AdminMediaService
import com.morningalarm.server.modules.media.application.ports.MediaStorage
import com.morningalarm.server.modules.media.infra.FirebaseMediaStorage
import com.morningalarm.server.modules.media.infra.LocalDevMediaStorage
import com.morningalarm.server.modules.ringtone.application.RingtoneService
import com.morningalarm.server.modules.ringtone.application.ports.RingtoneRepository
import com.morningalarm.server.modules.ringtone.infra.PostgresRingtoneRepository
import com.morningalarm.server.modules.ringtone.infra.RingtoneDatabaseSchema
import com.morningalarm.server.modules.user.infra.PostgresBusinessUserRepository
import com.morningalarm.server.modules.user.infra.UserDatabaseSchema
import com.morningalarm.server.shared.audit.AuditLogger
import com.morningalarm.server.shared.audit.Slf4jAuditLogger
import com.morningalarm.server.shared.auth.JwtAccessTokenService
import com.morningalarm.server.shared.persistence.JdbcSessionManager
import com.morningalarm.server.shared.ratelimit.BruteForceProtector
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

data class ModuleDependencies(
    val adminBootstrapService: AdminBootstrapService,
    val authService: AuthService,
    val authUserRepository: AuthUserRepository,
    val businessUserRepository: BusinessUserRepository,
    val authEmailGateway: AuthEmailGateway,
    val ringtoneService: RingtoneService,
    val adminMediaService: AdminMediaService,
    val accessTokenService: AccessTokenService,
    val dataSource: DataSource,
    val adminAccessSecret: String?,
)

fun createModuleDependencies(config: AppConfig): ModuleDependencies {
    val dataSource = createDataSource(config)
    ServerSchemaBootstrap(
        steps = listOf(
            AuthDatabaseSchema(dataSource),
            UserDatabaseSchema(dataSource),
            RingtoneDatabaseSchema(dataSource),
        ),
    ).ensureCreated()

    val sessionManager = JdbcSessionManager(dataSource)
    val auditLogger: AuditLogger = Slf4jAuditLogger()
    val clock: Clock = JavaClock()
    val repository: AuthUserRepository = PostgresAuthUserRepository(sessionManager)
    val businessUserRepository: BusinessUserRepository = PostgresBusinessUserRepository(sessionManager)
    val emailGateway = InMemoryAuthEmailGateway()
    val tokenFactory: TokenFactory = SecureTokenFactory()
    val passwordHasher: PasswordHasher = Pbkdf2PasswordHasher()
    val mediaStorage: MediaStorage = if (config.devMode) {
        LocalDevMediaStorage(
            storageDir = config.mediaStorageDir,
            publicBaseUrl = config.mediaPublicBaseUrl,
        )
    } else {
        FirebaseMediaStorage(
            credentialsPath = requireNotNull(config.firebaseCredentialsPath) {
                "SERVER_FIREBASE_CREDENTIALS or GOOGLE_APPLICATION_CREDENTIALS must be set in prod mode"
            },
            bucketName = requireNotNull(config.firebaseBucketName) {
                "SERVER_FIREBASE_BUCKET must be set in prod mode"
            },
        )
    }
    val adminMediaService = AdminMediaService(
        mediaStorage = mediaStorage,
        maxImageBytes = config.mediaMaxImageBytes,
        maxAudioBytes = config.mediaMaxAudioBytes,
        auditLogger = auditLogger,
    )
    val accessTokenService: AccessTokenService = JwtAccessTokenService(
        secret = config.jwtSecret,
        issuer = config.jwtIssuer,
        audience = config.jwtAudience,
    )
    val ringtoneRepository: RingtoneRepository = PostgresRingtoneRepository(sessionManager)

    // Brute-force protection for admin login: limited attempts within a sliding window
    val adminLoginBruteForce = BruteForceProtector(
        maxAttempts = config.adminLoginMaxAttempts,
        windowSeconds = config.adminLoginWindowSeconds,
    )

    val authSessionManager = AuthSessionManager(
        authUserRepository = repository,
        businessUserRepository = businessUserRepository,
        tokenFactory = tokenFactory,
        accessTokenService = accessTokenService,
        clock = clock,
        adminEmails = config.adminEmails,
        accessTokenTtlSeconds = config.accessTokenTtlSeconds,
        refreshTokenTtlSeconds = config.refreshTokenTtlSeconds,
    )
    val emailAuthService = EmailAuthService(
        authUserRepository = repository,
        authEmailGateway = emailGateway,
        tokenFactory = tokenFactory,
        passwordHasher = passwordHasher,
        auditLogger = auditLogger,
        transactionRunner = sessionManager,
        sessionManager = authSessionManager,
        passwordResetTokenTtlSeconds = config.passwordResetTokenTtlSeconds,
    )
    val adminLoginService = AdminLoginService(
        emailAuthService = emailAuthService,
        auditLogger = auditLogger,
        adminLoginBruteForce = adminLoginBruteForce,
    )
    val authService = AuthService(
        adminLoginService = adminLoginService,
        socialAuthService = SocialAuthService(
            authUserRepository = repository,
            tokenFactory = tokenFactory,
            transactionRunner = sessionManager,
            sessionManager = authSessionManager,
        ),
        emailAuthService = emailAuthService,
    )
    val adminBootstrapService = AdminBootstrapService(
        authUserRepository = repository,
        businessUserRepository = businessUserRepository,
        tokenFactory = tokenFactory,
        passwordHasher = passwordHasher,
        auditLogger = auditLogger,
        transactionRunner = sessionManager,
    )
    val ringtoneService = RingtoneService(
        ringtoneRepository = ringtoneRepository,
        tokenFactory = tokenFactory,
        auditLogger = auditLogger,
        clock = clock,
    )

    return ModuleDependencies(
        adminBootstrapService = adminBootstrapService,
        authService = authService,
        authUserRepository = repository,
        businessUserRepository = businessUserRepository,
        authEmailGateway = emailGateway,
        ringtoneService = ringtoneService,
        adminMediaService = adminMediaService,
        accessTokenService = accessTokenService,
        dataSource = dataSource,
        adminAccessSecret = config.adminAccessSecret,
    )
}

private fun createDataSource(config: AppConfig): DataSource {
    val hikariConfig = HikariConfig().apply {
        jdbcUrl = config.databaseUrl
        username = config.databaseUser
        password = config.databasePassword
        driverClassName = config.databaseDriver
        maximumPoolSize = config.databasePoolMaxSize
        minimumIdle = 1
        isAutoCommit = true
        connectionTimeout = 10_000
        initializationFailTimeout = 10_000
        validate()
    }
    return HikariDataSource(hikariConfig)
}
