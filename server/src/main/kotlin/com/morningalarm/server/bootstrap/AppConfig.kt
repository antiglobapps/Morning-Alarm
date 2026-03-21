package com.morningalarm.server.bootstrap

data class AppConfig(
    val host: String,
    val port: Int,
    val publicUrl: String?,
    val logPublicUrl: Boolean,
    val mediaStorageDir: String,
    val mediaPublicBaseUrl: String,
    val mediaMaxImageBytes: Long,
    val mediaMaxAudioBytes: Long,
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
)

object AppConfigLoader {
    fun fromEnv(env: Map<String, String> = System.getenv()): AppConfig {
        val port = env["SERVER_PORT"]?.toIntOrNull()
            ?: env["PORT"]?.toIntOrNull()
            ?: 8080

        return AppConfig(
            host = env["SERVER_HOST"] ?: "0.0.0.0",
            port = port,
            publicUrl = env["SERVER_PUBLIC_URL"],
            logPublicUrl = env["SERVER_LOG_PUBLIC_URL"].toBooleanFlag(),
            mediaStorageDir = env["SERVER_MEDIA_STORAGE_DIR"] ?: "./server-data/media",
            mediaPublicBaseUrl = env["SERVER_MEDIA_PUBLIC_BASE_URL"]
                ?: (env["SERVER_PUBLIC_URL"] ?: "http://localhost:$port"),
            mediaMaxImageBytes = env["SERVER_MEDIA_MAX_IMAGE_BYTES"]?.toLongOrNull() ?: 5L * 1024 * 1024,
            mediaMaxAudioBytes = env["SERVER_MEDIA_MAX_AUDIO_BYTES"]?.toLongOrNull() ?: 50L * 1024 * 1024,
            databaseUrl = env["SERVER_DB_URL"] ?: "jdbc:postgresql://localhost:5432/morning_alarm",
            databaseUser = env["SERVER_DB_USER"] ?: "morning_alarm",
            databasePassword = env["SERVER_DB_PASSWORD"] ?: "morning_alarm",
            databaseDriver = env["SERVER_DB_DRIVER"] ?: "org.postgresql.Driver",
            databasePoolMaxSize = env["SERVER_DB_POOL_MAX_SIZE"]?.toIntOrNull() ?: 10,
            jwtSecret = env["SERVER_JWT_SECRET"] ?: "dev-only-jwt-secret-change-me",
            jwtIssuer = env["SERVER_JWT_ISSUER"] ?: "morning-alarm-server",
            jwtAudience = env["SERVER_JWT_AUDIENCE"] ?: "morning-alarm-app",
            adminEmails = env["SERVER_ADMIN_EMAILS"]
                ?.split(',')
                ?.map { it.trim().lowercase() }
                ?.filter { it.isNotBlank() }
                ?.toSet()
                ?: emptySet(),
            adminBootstrapSecret = env["SERVER_ADMIN_BOOTSTRAP_SECRET"],
            adminAccessSecret = env["SERVER_ADMIN_ACCESS_SECRET"],
            accessTokenTtlSeconds = 24 * 60 * 60L,
            refreshTokenTtlSeconds = 30 * 24 * 60 * 60L,
            passwordResetTokenTtlSeconds = 60 * 60L,
        )
    }
}

private fun String?.toBooleanFlag(): Boolean {
    return when (this?.trim()?.lowercase()) {
        "1", "true", "yes", "y", "on" -> true
        else -> false
    }
}
