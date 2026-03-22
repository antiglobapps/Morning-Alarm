package com.morningalarm.server.modules.auth

import com.morningalarm.server.createTestDataSource
import com.morningalarm.server.createTestDataDir
import com.morningalarm.server.modules.auth.domain.AuthUser
import com.morningalarm.server.modules.auth.domain.PasswordResetTokenRecord
import com.morningalarm.server.modules.auth.domain.RefreshTokenRecord
import com.morningalarm.server.modules.auth.domain.SocialAccount
import com.morningalarm.server.modules.auth.domain.SocialProvider
import com.morningalarm.server.modules.auth.infra.AuthDatabaseSchema
import com.morningalarm.server.modules.auth.infra.PostgresAuthUserRepository
import com.morningalarm.server.shared.persistence.JdbcSessionManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PostgresAuthUserRepositoryTest {
    @Test
    fun `upsert user replaces social accounts and social lookup follows latest mapping`() {
        createTestDataSource(createTestDataDir("morning-alarm-auth-repository")).use { dataSource ->
            AuthDatabaseSchema(dataSource).ensureCreated()
            val repository = PostgresAuthUserRepository(JdbcSessionManager(dataSource))

            repository.upsertUser(
                AuthUser(
                    id = "usr_1",
                    email = "user@example.com",
                    displayName = "User",
                    passwordHash = "hash",
                    socialAccounts = setOf(SocialAccount(SocialProvider.GOOGLE, "sub-1")),
                ),
            )

            val firstLookup = requireNotNull(repository.findBySocialAccount(SocialProvider.GOOGLE, "sub-1"))
            assertEquals("usr_1", firstLookup.id)

            repository.upsertUser(
                AuthUser(
                    id = "usr_1",
                    email = "user@example.com",
                    displayName = "User",
                    passwordHash = "hash",
                    socialAccounts = setOf(SocialAccount(SocialProvider.GOOGLE, "sub-2")),
                ),
            )

            assertNull(repository.findBySocialAccount(SocialProvider.GOOGLE, "sub-1"))
            val secondLookup = requireNotNull(repository.findBySocialAccount(SocialProvider.GOOGLE, "sub-2"))
            assertEquals("usr_1", secondLookup.id)
        }
    }

    @Test
    fun `refresh and password reset tokens are one-shot`() {
        createTestDataSource(createTestDataDir("morning-alarm-auth-token-repository")).use { dataSource ->
            AuthDatabaseSchema(dataSource).ensureCreated()
            val repository = PostgresAuthUserRepository(JdbcSessionManager(dataSource))

            repository.upsertUser(
                AuthUser(
                    id = "usr_1",
                    email = "user@example.com",
                    displayName = "User",
                    passwordHash = "hash",
                    socialAccounts = emptySet(),
                ),
            )

            repository.saveRefreshToken(RefreshTokenRecord(token = "refresh-1", userId = "usr_1", expiresAtEpochSeconds = 100))
            assertEquals("usr_1", requireNotNull(repository.consumeRefreshToken("refresh-1")).userId)
            assertNull(repository.consumeRefreshToken("refresh-1"))

            repository.savePasswordResetToken(
                PasswordResetTokenRecord(
                    token = "reset-1",
                    userId = "usr_1",
                    email = "user@example.com",
                    expiresAtEpochSeconds = 100,
                ),
            )
            assertEquals("usr_1", requireNotNull(repository.consumePasswordResetToken("reset-1")).userId)
            assertNull(repository.consumePasswordResetToken("reset-1"))
        }
    }
}
