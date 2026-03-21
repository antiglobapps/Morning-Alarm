package com.morningalarm.server.bootstrap

import com.morningalarm.api.auth.DevAdminDefaults

data class AppConfig(
    val devMode: Boolean,
    val host: String,
    val port: Int,
    val publicUrl: String?,
    val logPublicUrl: Boolean,
    val mediaStorageDir: String,
    val mediaPublicBaseUrl: String,
    val mediaMaxImageBytes: Long,
    val mediaMaxAudioBytes: Long,
    /** Firebase Storage bucket name (required in prod mode). */
    val firebaseBucketName: String?,
    /** Path to Firebase service account JSON credentials (required in prod mode). */
    val firebaseCredentialsPath: String?,
    val databaseUrl: String,
    val databaseUser: String,
    val databasePassword: String,
    val databaseDriver: String,
    val databasePoolMaxSize: Int,
    val jwtSecret: String,
    val jwtIssuer: String,
    val jwtAudience: String,
    val adminEmails: Set<String>,
    /** Secret required to bootstrap/promote admin via CLI. Must be set in prod. */
    val adminBootstrapSecret: String?,
    /** Secret required in X-Admin-Secret header for admin API access. Must be set in prod. */
    val adminAccessSecret: String?,
    val accessTokenTtlSeconds: Long,
    val refreshTokenTtlSeconds: Long,
    val passwordResetTokenTtlSeconds: Long,
    /** Max failed admin login attempts within the rate limit window before blocking. */
    val adminLoginMaxAttempts: Int,
    /** Sliding window size in seconds for admin login brute-force protection. */
    val adminLoginWindowSeconds: Long,
) {
    init {
        if (!devMode) {
            require(jwtSecret != "dev-only-jwt-secret-change-me") {
                "SERVER_JWT_SECRET must be set to a secure value in prod mode"
            }
            requireNotNull(adminAccessSecret) {
                "SERVER_ADMIN_ACCESS_SECRET must be set in prod mode"
            }
        }
    }
}

object AppConfigLoader {
    private const val H2_DEV_URL =
        "jdbc:h2:file:./server-data/dev-db;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
    private const val H2_DRIVER = "org.h2.Driver"

    fun fromEnv(env: Map<String, String> = System.getenv()): AppConfig {
        val devMode = env["SERVER_DEV_MODE"]?.toBooleanFlag() ?: true

        val port = env["SERVER_PORT"]?.toIntOrNull()
            ?: env["PORT"]?.toIntOrNull()
            ?: 8080

        return AppConfig(
            devMode = devMode,
            host = env["SERVER_HOST"] ?: "0.0.0.0",
            port = port,
            publicUrl = env["SERVER_PUBLIC_URL"],
            logPublicUrl = env["SERVER_LOG_PUBLIC_URL"].toBooleanFlag(),
            mediaStorageDir = env["SERVER_MEDIA_STORAGE_DIR"] ?: "./server-data/media",
            mediaPublicBaseUrl = env["SERVER_MEDIA_PUBLIC_BASE_URL"]
                ?: (env["SERVER_PUBLIC_URL"] ?: "http://localhost:$port"),
            mediaMaxImageBytes = env["SERVER_MEDIA_MAX_IMAGE_BYTES"]?.toLongOrNull() ?: 5L * 1024 * 1024,
            mediaMaxAudioBytes = env["SERVER_MEDIA_MAX_AUDIO_BYTES"]?.toLongOrNull() ?: 50L * 1024 * 1024,
            firebaseBucketName = env["SERVER_FIREBASE_BUCKET"],
            firebaseCredentialsPath = env["SERVER_FIREBASE_CREDENTIALS"]
                ?: env["GOOGLE_APPLICATION_CREDENTIALS"],
            databaseUrl = env["SERVER_DB_URL"]
                ?: if (devMode) H2_DEV_URL else "jdbc:postgresql://localhost:5432/morning_alarm",
            databaseUser = env["SERVER_DB_USER"]
                ?: if (devMode) "sa" else "morning_alarm",
            databasePassword = env["SERVER_DB_PASSWORD"]
                ?: if (devMode) "" else "morning_alarm",
            databaseDriver = env["SERVER_DB_DRIVER"]
                ?: if (devMode) H2_DRIVER else "org.postgresql.Driver",
            databasePoolMaxSize = env["SERVER_DB_POOL_MAX_SIZE"]?.toIntOrNull() ?: 10,
            jwtSecret = env["SERVER_JWT_SECRET"] ?: "dev-only-jwt-secret-change-me",
            jwtIssuer = env["SERVER_JWT_ISSUER"] ?: "morning-alarm-server",
            jwtAudience = env["SERVER_JWT_AUDIENCE"] ?: "morning-alarm-app",
            adminEmails = env["SERVER_ADMIN_EMAILS"]
                ?.split(',')
                ?.map { it.trim().lowercase() }
                ?.filter { it.isNotBlank() }
                ?.toSet()
                ?.let { if (devMode) it + DevAdminDefaults.EMAIL else it }
                ?: if (devMode) setOf(DevAdminDefaults.EMAIL) else emptySet(),
            adminBootstrapSecret = env["SERVER_ADMIN_BOOTSTRAP_SECRET"],
            adminAccessSecret = env["SERVER_ADMIN_ACCESS_SECRET"]
                ?.takeIf { it.isNotBlank() }
                ?: if (devMode) DevAdminDefaults.ACCESS_SECRET else null,
            accessTokenTtlSeconds = 24 * 60 * 60L,
            refreshTokenTtlSeconds = 30 * 24 * 60 * 60L,
            passwordResetTokenTtlSeconds = 60 * 60L,
            adminLoginMaxAttempts = env["SERVER_ADMIN_LOGIN_MAX_ATTEMPTS"]?.toIntOrNull() ?: 5,
            adminLoginWindowSeconds = env["SERVER_ADMIN_LOGIN_WINDOW_SECONDS"]?.toLongOrNull() ?: 300L,
        )
    }
}

private fun String?.toBooleanFlag(): Boolean {
    return when (this?.trim()?.lowercase()) {
        "1", "true", "yes", "y", "on" -> true
        else -> false
    }
}
