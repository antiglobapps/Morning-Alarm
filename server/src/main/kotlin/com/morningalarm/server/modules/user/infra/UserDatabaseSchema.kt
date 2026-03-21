package com.morningalarm.server.modules.user.infra

import javax.sql.DataSource

class UserDatabaseSchema(
    private val dataSource: DataSource,
) {
    fun ensureCreated() {
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.execute(
                    """
                    CREATE TABLE IF NOT EXISTS business_users (
                        id VARCHAR(128) PRIMARY KEY REFERENCES auth_users(id) ON DELETE CASCADE,
                        email VARCHAR(320),
                        display_name VARCHAR(255),
                        role VARCHAR(32) NOT NULL
                    )
                    """.trimIndent(),
                )
            }
        }
    }
}
