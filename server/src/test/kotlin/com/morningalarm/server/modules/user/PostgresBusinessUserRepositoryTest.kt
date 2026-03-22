package com.morningalarm.server.modules.user

import com.morningalarm.server.createTestDataSource
import com.morningalarm.server.createTestDataDir
import com.morningalarm.server.modules.auth.domain.AuthUser
import com.morningalarm.server.modules.auth.domain.UserRole
import com.morningalarm.server.modules.auth.infra.AuthDatabaseSchema
import com.morningalarm.server.modules.auth.infra.PostgresAuthUserRepository
import com.morningalarm.server.modules.user.infra.PostgresBusinessUserRepository
import com.morningalarm.server.modules.user.infra.UserDatabaseSchema
import com.morningalarm.server.shared.persistence.JdbcSessionManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PostgresBusinessUserRepositoryTest {
    @Test
    fun `ensureUser updates existing row and updateRole persists role changes`() {
        createTestDataSource(createTestDataDir("morning-alarm-business-user-repository")).use { dataSource ->
            AuthDatabaseSchema(dataSource).ensureCreated()
            UserDatabaseSchema(dataSource).ensureCreated()
            val sessionManager = JdbcSessionManager(dataSource)
            val authUsers = PostgresAuthUserRepository(sessionManager)
            val repository = PostgresBusinessUserRepository(sessionManager)

            authUsers.upsertUser(
                AuthUser(
                    id = "usr_1",
                    email = "user@example.com",
                    displayName = "User",
                    passwordHash = "hash",
                    socialAccounts = emptySet(),
                ),
            )

            val created = repository.ensureUser(
                id = "usr_1",
                email = "user@example.com",
                displayName = "User",
                role = UserRole.USER,
            )
            assertEquals(UserRole.USER, created.role)

            val updated = repository.ensureUser(
                id = "usr_1",
                email = "user+updated@example.com",
                displayName = "Updated User",
                role = UserRole.ADMIN,
            )
            assertEquals("user+updated@example.com", updated.email)
            assertEquals("Updated User", updated.displayName)
            assertEquals(UserRole.ADMIN, updated.role)

            repository.updateRole("usr_1", UserRole.USER)
            val persisted = requireNotNull(repository.findById("usr_1"))
            assertEquals(UserRole.USER, persisted.role)
            assertEquals("user+updated@example.com", persisted.email)
            assertEquals("Updated User", persisted.displayName)
        }
    }
}
