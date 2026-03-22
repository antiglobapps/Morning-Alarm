package com.morningalarm.server.bootstrap

import com.morningalarm.server.modules.auth.infra.AuthDatabaseSchema
import com.morningalarm.server.modules.ringtone.infra.RingtoneDatabaseSchema
import com.morningalarm.server.modules.user.infra.UserDatabaseSchema
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertTrue

class ServerSchemaBootstrapTest {
    @Test
    fun `server schema bootstrap is idempotent and creates all core tables`() {
        val dataDir = Files.createTempDirectory("morning-alarm-server-schema").toString()
        createSchemaTestDataSource(dataDir).use { dataSource ->
            val bootstrap = ServerSchemaBootstrap(
                steps = listOf(
                    AuthDatabaseSchema(dataSource),
                    UserDatabaseSchema(dataSource),
                    RingtoneDatabaseSchema(dataSource),
                ),
            )

            bootstrap.ensureCreated()
            bootstrap.ensureCreated()

            dataSource.connection.use { connection ->
                assertTrue(connection.hasTable("auth_users"))
                assertTrue(connection.hasTable("business_users"))
                assertTrue(connection.hasTable("ringtones"))
                assertTrue(connection.hasTable("user_ringtone_likes"))
            }
        }
    }
}

private fun createSchemaTestDataSource(dataDir: String): HikariDataSource {
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

private fun java.sql.Connection.hasTable(tableName: String): Boolean {
    metaData.getTables(null, null, tableName, null).use { resultSet ->
        return resultSet.next()
    }
}
