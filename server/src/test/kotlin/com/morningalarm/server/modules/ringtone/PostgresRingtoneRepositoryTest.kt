package com.morningalarm.server.modules.ringtone

import com.morningalarm.server.createTestDataSource
import com.morningalarm.server.createTestDataDir
import com.morningalarm.server.modules.auth.domain.AuthUser
import com.morningalarm.server.modules.auth.domain.UserRole
import com.morningalarm.server.modules.auth.infra.AuthDatabaseSchema
import com.morningalarm.server.modules.auth.infra.PostgresAuthUserRepository
import com.morningalarm.server.modules.ringtone.domain.Ringtone
import com.morningalarm.server.modules.ringtone.domain.RingtoneListFilter
import com.morningalarm.server.modules.ringtone.domain.RingtoneVisibility
import com.morningalarm.server.modules.ringtone.infra.PostgresRingtoneRepository
import com.morningalarm.server.modules.ringtone.infra.RingtoneDatabaseSchema
import com.morningalarm.server.modules.user.infra.PostgresBusinessUserRepository
import com.morningalarm.server.modules.user.infra.UserDatabaseSchema
import com.morningalarm.server.shared.persistence.JdbcSessionManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PostgresRingtoneRepositoryTest {
    @Test
    fun `list for user respects visibility and ownership filters`() {
        createTestDataSource(createTestDataDir("morning-alarm-ringtone-repository")).use { dataSource ->
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
                    email = "owner@example.com",
                    displayName = "Owner",
                    passwordHash = null,
                    socialAccounts = emptySet(),
                ),
            )
            authUsers.upsertUser(
                AuthUser(
                    id = "user-2",
                    email = "other@example.com",
                    displayName = "Other",
                    passwordHash = null,
                    socialAccounts = emptySet(),
                ),
            )
            businessUsers.ensureUser("admin-1", "admin@example.com", "Admin", UserRole.ADMIN)
            businessUsers.ensureUser("user-1", "owner@example.com", "Owner", UserRole.USER)
            businessUsers.ensureUser("user-2", "other@example.com", "Other", UserRole.USER)

            repository.create(
                ringtone(
                    id = "rng-public",
                    title = "Public",
                    visibility = RingtoneVisibility.PUBLIC,
                    createdByAdminId = "admin-1",
                    createdByUserId = null,
                ),
            )
            repository.create(
                ringtone(
                    id = "rng-private",
                    title = "Private",
                    visibility = RingtoneVisibility.PRIVATE,
                    createdByAdminId = null,
                    createdByUserId = "user-1",
                ),
            )
            repository.create(
                ringtone(
                    id = "rng-inactive",
                    title = "Inactive",
                    visibility = RingtoneVisibility.INACTIVE,
                    createdByAdminId = "admin-1",
                    createdByUserId = null,
                ),
            )

            assertEquals(listOf("rng-private", "rng-public"), repository.listForUser("user-1", RingtoneListFilter.ALL).map { it.ringtone.id })
            assertEquals(listOf("rng-private"), repository.listForUser("user-1", RingtoneListFilter.MY).map { it.ringtone.id })
            assertEquals(listOf("rng-public"), repository.listForUser("user-1", RingtoneListFilter.SYSTEM).map { it.ringtone.id })

            assertEquals(listOf("rng-public"), repository.listForUser("user-2", RingtoneListFilter.ALL).map { it.ringtone.id })
            assertTrue(repository.findForUser("user-2", "rng-private") == null)
        }
    }

    private fun ringtone(
        id: String,
        title: String,
        visibility: RingtoneVisibility,
        createdByAdminId: String?,
        createdByUserId: String?,
    ) = Ringtone(
        id = id,
        title = title,
        imageUrl = "https://cdn.example.com/$id.jpg",
        audioUrl = "https://cdn.example.com/$id.mp3",
        durationSeconds = 30,
        description = "$title description",
        visibility = visibility,
        isPremium = false,
        createdAtEpochSeconds = 1,
        updatedAtEpochSeconds = 1,
        createdByAdminId = createdByAdminId,
        updatedByAdminId = createdByAdminId,
        createdByUserId = createdByUserId,
    )
}
