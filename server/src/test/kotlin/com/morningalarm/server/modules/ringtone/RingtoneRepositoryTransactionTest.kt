package com.morningalarm.server.modules.ringtone

import com.morningalarm.server.modules.auth.domain.UserRole
import com.morningalarm.server.modules.auth.domain.AuthUser
import com.morningalarm.server.modules.auth.infra.AuthDatabaseSchema
import com.morningalarm.server.modules.auth.infra.PostgresAuthUserRepository
import com.morningalarm.server.modules.ringtone.domain.Ringtone
import com.morningalarm.server.modules.ringtone.domain.RingtoneVisibility
import com.morningalarm.server.modules.ringtone.infra.PostgresRingtoneRepository
import com.morningalarm.server.modules.ringtone.infra.RingtoneDatabaseSchema
import com.morningalarm.server.modules.user.infra.PostgresBusinessUserRepository
import com.morningalarm.server.modules.user.infra.UserDatabaseSchema
import com.morningalarm.server.shared.persistence.JdbcSessionManager
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RingtoneRepositoryTransactionTest {
    @Test
    fun `toggle like participates in outer transaction and rolls back on failure`() {
        val dataDir = Files.createTempDirectory("morning-alarm-ringtone-transaction").toString()
        createTestDataSource(dataDir).use { dataSource ->
            AuthDatabaseSchema(dataSource).ensureCreated()
            UserDatabaseSchema(dataSource).ensureCreated()
            RingtoneDatabaseSchema(dataSource).ensureCreated()

            val sessionManager = JdbcSessionManager(dataSource)
            val authUsers = PostgresAuthUserRepository(sessionManager)
            val businessUsers = PostgresBusinessUserRepository(sessionManager)
            val repository = PostgresRingtoneRepository(sessionManager)

            authUsers.upsertUser(
                AuthUser(
                    id = "admin-1",
                    email = "admin@example.com",
                    displayName = "Admin",
                    passwordHash = null,
                    socialAccounts = emptySet(),
                ),
            )
            authUsers.upsertUser(
                AuthUser(
                    id = "user-1",
                    email = "user@example.com",
                    displayName = "User",
                    passwordHash = null,
                    socialAccounts = emptySet(),
                ),
            )
            businessUsers.ensureUser(
                id = "admin-1",
                email = "admin@example.com",
                displayName = "Admin",
                role = UserRole.ADMIN,
            )
            businessUsers.ensureUser(
                id = "user-1",
                email = "user@example.com",
                displayName = "User",
                role = UserRole.USER,
            )
            repository.create(
                Ringtone(
                    id = "rng-1",
                    title = "Rollback Bell",
                    imageUrl = "https://cdn.example.com/ringtones/rollback.jpg",
                    audioUrl = "https://cdn.example.com/ringtones/rollback.mp3",
                    durationSeconds = 30,
                    description = "Rollback test ringtone",
                    visibility = RingtoneVisibility.PUBLIC,
                    isPremium = false,
                    createdAtEpochSeconds = 1,
                    updatedAtEpochSeconds = 1,
                    createdByAdminId = "admin-1",
                    updatedByAdminId = "admin-1",
                    createdByUserId = null,
                ),
            )

            assertFailsWith<IllegalStateException> {
                sessionManager.inTransaction {
                    val result = repository.toggleLike(userId = "user-1", ringtoneId = "rng-1")
                    assertTrue(requireNotNull(result).isLikedByUser)
                    assertEquals(1, result.likesCount)
                    throw IllegalStateException("Rollback outer transaction")
                }
            }

            val ringtoneAfterRollback = requireNotNull(repository.findForUser(userId = "user-1", ringtoneId = "rng-1"))
            assertFalse(ringtoneAfterRollback.isLikedByUser)
            assertEquals(0, ringtoneAfterRollback.likesCount)
        }
    }
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
