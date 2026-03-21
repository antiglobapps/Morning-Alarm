package com.morningalarm.server.modules.auth.infra

import javax.sql.DataSource

class AuthDatabaseSchema(
    private val dataSource: DataSource,
) {
    fun ensureCreated() {
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                schemaStatements.forEach(statement::execute)
            }
        }
    }

    private companion object {
        val schemaStatements = listOf(
            """
            CREATE TABLE IF NOT EXISTS auth_users (
                id VARCHAR(128) PRIMARY KEY,
                email VARCHAR(320) UNIQUE,
                display_name VARCHAR(255),
                password_hash TEXT
            )
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS auth_social_accounts (
                provider VARCHAR(64) NOT NULL,
                external_subject VARCHAR(512) NOT NULL,
                user_id VARCHAR(128) NOT NULL REFERENCES auth_users(id) ON DELETE CASCADE,
                PRIMARY KEY (provider, external_subject)
            )
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS auth_refresh_tokens (
                token VARCHAR(512) PRIMARY KEY,
                user_id VARCHAR(128) NOT NULL REFERENCES auth_users(id) ON DELETE CASCADE,
                expires_at_epoch_seconds BIGINT NOT NULL
            )
            """.trimIndent(),
            """
            CREATE TABLE IF NOT EXISTS auth_password_reset_tokens (
                token VARCHAR(512) PRIMARY KEY,
                user_id VARCHAR(128) NOT NULL REFERENCES auth_users(id) ON DELETE CASCADE,
                email VARCHAR(320) NOT NULL,
                expires_at_epoch_seconds BIGINT NOT NULL
            )
            """.trimIndent(),
            "CREATE INDEX IF NOT EXISTS idx_auth_users_email ON auth_users(email)",
            "CREATE INDEX IF NOT EXISTS idx_auth_social_accounts_user_id ON auth_social_accounts(user_id)",
            "CREATE INDEX IF NOT EXISTS idx_auth_refresh_tokens_user_id ON auth_refresh_tokens(user_id)",
            "CREATE INDEX IF NOT EXISTS idx_auth_password_reset_tokens_user_id ON auth_password_reset_tokens(user_id)",
        )
    }
}
