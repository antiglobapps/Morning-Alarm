package com.morningalarm.server.modules.ringtone

import com.morningalarm.server.modules.auth.infra.AuthDatabaseSchema
import com.morningalarm.server.modules.ringtone.infra.RingtoneDatabaseSchema
import com.morningalarm.server.modules.user.infra.UserDatabaseSchema
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RingtoneDatabaseSchemaTest {
    @Test
    fun `ensureCreated upgrades legacy ringtone schema with is_active to visibility`() {
        createRingtoneSchemaTestDataSource().use { dataSource ->
            AuthDatabaseSchema(dataSource).ensureCreated()
            UserDatabaseSchema(dataSource).ensureCreated()

            dataSource.connection.use { connection ->
                connection.createStatement().use { statement ->
                    statement.execute(
                        """
                        CREATE TABLE ringtones (
                            id VARCHAR(128) PRIMARY KEY,
                            title VARCHAR(255) NOT NULL,
                            image_url TEXT NOT NULL,
                            audio_url TEXT NOT NULL,
                            duration_seconds INTEGER NOT NULL,
                            description TEXT NOT NULL,
                            is_active BOOLEAN NOT NULL,
                            is_premium BOOLEAN NOT NULL,
                            created_at_epoch_seconds BIGINT NOT NULL,
                            updated_at_epoch_seconds BIGINT NOT NULL,
                            created_by_admin_id VARCHAR(128) NOT NULL REFERENCES business_users(id),
                            updated_by_admin_id VARCHAR(128) NOT NULL REFERENCES business_users(id)
                        )
                        """.trimIndent(),
                    )
                    statement.execute(
                        """
                        INSERT INTO auth_users (id, email, display_name, password_hash)
                        VALUES ('usr_admin', 'admin@example.com', 'Admin', 'hash')
                        """.trimIndent(),
                    )
                    statement.execute(
                        """
                        INSERT INTO business_users (id, email, display_name, role)
                        VALUES ('usr_admin', 'admin@example.com', 'Admin', 'ADMIN')
                        """.trimIndent(),
                    )
                    statement.execute(
                        """
                        INSERT INTO ringtones (
                            id, title, image_url, audio_url, duration_seconds, description,
                            is_active, is_premium, created_at_epoch_seconds, updated_at_epoch_seconds,
                            created_by_admin_id, updated_by_admin_id
                        ) VALUES (
                            'rng_legacy', 'Legacy ringtone', 'https://cdn.example.com/legacy.jpg',
                            'https://cdn.example.com/legacy.mp3', 30, 'Legacy description',
                            TRUE, FALSE, 1, 1, 'usr_admin', 'usr_admin'
                        )
                        """.trimIndent(),
                    )
                }
            }

            RingtoneDatabaseSchema(dataSource).ensureCreated()

            dataSource.connection.use { connection ->
                connection.prepareStatement(
                    """
                    SELECT visibility, created_by_user_id
                    FROM ringtones
                    WHERE id = 'rng_legacy'
                    """.trimIndent(),
                ).use { statement ->
                    statement.executeQuery().use { resultSet ->
                        assertTrue(resultSet.next())
                        assertEquals("PUBLIC", resultSet.getString("visibility"))
                        assertEquals(null, resultSet.getString("created_by_user_id"))
                    }
                }
            }
        }
    }
}

private fun createRingtoneSchemaTestDataSource(): HikariDataSource {
    val dataDir = Files.createTempDirectory("morning-alarm-ringtone-schema").toString()
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
