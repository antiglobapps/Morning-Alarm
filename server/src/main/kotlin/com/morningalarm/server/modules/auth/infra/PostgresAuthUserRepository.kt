package com.morningalarm.server.modules.auth.infra

import com.morningalarm.server.modules.auth.application.ports.AuthUserRepository
import com.morningalarm.server.modules.auth.domain.AuthUser
import com.morningalarm.server.modules.auth.domain.PasswordResetTokenRecord
import com.morningalarm.server.modules.auth.domain.RefreshTokenRecord
import com.morningalarm.server.modules.auth.domain.SocialAccount
import com.morningalarm.server.modules.auth.domain.SocialProvider
import com.morningalarm.server.shared.persistence.JdbcSessionManager
import java.sql.Connection

class PostgresAuthUserRepository(
    private val sessionManager: JdbcSessionManager,
) : AuthUserRepository {
    override fun findByEmail(email: String): AuthUser? {
        return sessionManager.withConnection { connection ->
            val userId = connection.prepareStatement(
                "SELECT id FROM auth_users WHERE email = ?",
            ).use { statement ->
                statement.setString(1, email)
                statement.executeQuery().use { rs -> if (rs.next()) rs.getString("id") else null }
            } ?: return@withConnection null
            loadUser(connection, userId)
        }
    }

    override fun findBySocialAccount(provider: SocialProvider, externalSubject: String): AuthUser? {
        return sessionManager.withConnection { connection ->
            connection.prepareStatement(
                """
                SELECT user_id
                FROM auth_social_accounts
                WHERE provider = ? AND external_subject = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, provider.name)
                statement.setString(2, externalSubject)
                statement.executeQuery().use { resultSet ->
                    if (!resultSet.next()) {
                        return@use null
                    }
                    loadUser(connection, resultSet.getString("user_id"))
                }
            }
        }
    }

    override fun findById(userId: String): AuthUser? {
        return sessionManager.withConnection { connection ->
            loadUser(connection, userId)
        }
    }

    override fun countUsers(): Long {
        return sessionManager.withConnection { connection ->
            connection.prepareStatement("SELECT COUNT(*) FROM auth_users").use { statement ->
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) resultSet.getLong(1) else 0L
                }
            }
        }
    }

    override fun upsertUser(user: AuthUser): AuthUser {
        return sessionManager.inTransaction {
            sessionManager.withConnection { connection ->
                val existing = selectUserRow(connection, user.id)
                if (existing == null) {
                    insertUser(connection, user)
                } else {
                    updateUser(connection, user)
                    deleteSocialAccounts(connection, user.id)
                }
                insertSocialAccounts(connection, user)
                user
            }
        }
    }

    override fun savePasswordResetToken(record: PasswordResetTokenRecord) {
        sessionManager.inTransaction {
            sessionManager.withConnection { connection ->
                connection.prepareStatement(
                    "DELETE FROM auth_password_reset_tokens WHERE token = ?",
                ).use { statement ->
                    statement.setString(1, record.token)
                    statement.executeUpdate()
                }
                connection.prepareStatement(
                    """
                    INSERT INTO auth_password_reset_tokens (token, user_id, email, expires_at_epoch_seconds)
                    VALUES (?, ?, ?, ?)
                    """.trimIndent(),
                ).use { statement ->
                    statement.setString(1, record.token)
                    statement.setString(2, record.userId)
                    statement.setString(3, record.email)
                    statement.setLong(4, record.expiresAtEpochSeconds)
                    statement.executeUpdate()
                }
            }
        }
    }

    override fun consumePasswordResetToken(token: String): PasswordResetTokenRecord? {
        return sessionManager.inTransaction {
            sessionManager.withConnection { connection ->
                val record = connection.prepareStatement(
                    """
                    SELECT token, user_id, email, expires_at_epoch_seconds
                    FROM auth_password_reset_tokens
                    WHERE token = ?
                    """.trimIndent(),
                ).use { statement ->
                    statement.setString(1, token)
                    statement.executeQuery().use { resultSet ->
                        if (!resultSet.next()) {
                            null
                        } else {
                            PasswordResetTokenRecord(
                                token = resultSet.getString("token"),
                                userId = resultSet.getString("user_id"),
                                email = resultSet.getString("email"),
                                expiresAtEpochSeconds = resultSet.getLong("expires_at_epoch_seconds"),
                            )
                        }
                    }
                } ?: return@withConnection null

                connection.prepareStatement(
                    "DELETE FROM auth_password_reset_tokens WHERE token = ?",
                ).use { statement ->
                    statement.setString(1, token)
                    statement.executeUpdate()
                }
                record
            }
        }
    }

    override fun saveRefreshToken(record: RefreshTokenRecord) {
        sessionManager.inTransaction {
            sessionManager.withConnection { connection ->
                connection.prepareStatement(
                    "DELETE FROM auth_refresh_tokens WHERE token = ?",
                ).use { statement ->
                    statement.setString(1, record.token)
                    statement.executeUpdate()
                }
                connection.prepareStatement(
                    """
                    INSERT INTO auth_refresh_tokens (token, user_id, expires_at_epoch_seconds)
                    VALUES (?, ?, ?)
                    """.trimIndent(),
                ).use { statement ->
                    statement.setString(1, record.token)
                    statement.setString(2, record.userId)
                    statement.setLong(3, record.expiresAtEpochSeconds)
                    statement.executeUpdate()
                }
            }
        }
    }

    override fun revokeAllRefreshTokens(userId: String) {
        sessionManager.withConnection { connection ->
            connection.prepareStatement(
                "DELETE FROM auth_refresh_tokens WHERE user_id = ?",
            ).use { statement ->
                statement.setString(1, userId)
                statement.executeUpdate()
            }
        }
    }

    override fun consumeRefreshToken(token: String): RefreshTokenRecord? {
        return sessionManager.inTransaction {
            sessionManager.withConnection { connection ->
                val record = connection.prepareStatement(
                    """
                    SELECT token, user_id, expires_at_epoch_seconds
                    FROM auth_refresh_tokens
                    WHERE token = ?
                    """.trimIndent(),
                ).use { statement ->
                    statement.setString(1, token)
                    statement.executeQuery().use { resultSet ->
                        if (!resultSet.next()) {
                            null
                        } else {
                            RefreshTokenRecord(
                                token = resultSet.getString("token"),
                                userId = resultSet.getString("user_id"),
                                expiresAtEpochSeconds = resultSet.getLong("expires_at_epoch_seconds"),
                            )
                        }
                    }
                } ?: return@withConnection null

                connection.prepareStatement(
                    "DELETE FROM auth_refresh_tokens WHERE token = ?",
                ).use { statement ->
                    statement.setString(1, token)
                    statement.executeUpdate()
                }
                record
            }
        }
    }

    private fun loadUser(connection: Connection, userId: String): AuthUser? {
        val userRow = selectUserRow(connection, userId) ?: return null
        val socialAccounts = connection.prepareStatement(
            """
            SELECT provider, external_subject
            FROM auth_social_accounts
            WHERE user_id = ?
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, userId)
            statement.executeQuery().use { resultSet ->
                buildSet {
                    while (resultSet.next()) {
                        add(
                            SocialAccount(
                                provider = SocialProvider.valueOf(resultSet.getString("provider")),
                                externalSubject = resultSet.getString("external_subject"),
                            ),
                        )
                    }
                }
            }
        }

        return AuthUser(
            id = userRow.id,
            email = userRow.email,
            displayName = userRow.displayName,
            passwordHash = userRow.passwordHash,
            socialAccounts = socialAccounts,
        )
    }

    private fun selectUserRow(connection: Connection, userId: String): UserRow? {
        return connection.prepareStatement(
            """
            SELECT id, email, display_name, password_hash
            FROM auth_users
            WHERE id = ?
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, userId)
            statement.executeQuery().use { resultSet ->
                if (!resultSet.next()) {
                    null
                } else {
                    UserRow(
                        id = resultSet.getString("id"),
                        email = resultSet.getString("email"),
                        displayName = resultSet.getString("display_name"),
                        passwordHash = resultSet.getString("password_hash"),
                    )
                }
            }
        }
    }

    private fun insertUser(connection: Connection, user: AuthUser) {
        connection.prepareStatement(
            """
            INSERT INTO auth_users (id, email, display_name, password_hash)
            VALUES (?, ?, ?, ?)
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, user.id)
            statement.setString(2, user.email)
            statement.setString(3, user.displayName)
            statement.setString(4, user.passwordHash)
            statement.executeUpdate()
        }
    }

    private fun updateUser(connection: Connection, user: AuthUser) {
        connection.prepareStatement(
            """
            UPDATE auth_users
            SET email = ?, display_name = ?, password_hash = ?
            WHERE id = ?
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, user.email)
            statement.setString(2, user.displayName)
            statement.setString(3, user.passwordHash)
            statement.setString(4, user.id)
            statement.executeUpdate()
        }
    }

    private fun deleteSocialAccounts(connection: Connection, userId: String) {
        connection.prepareStatement(
            "DELETE FROM auth_social_accounts WHERE user_id = ?",
        ).use { statement ->
            statement.setString(1, userId)
            statement.executeUpdate()
        }
    }

    private fun insertSocialAccounts(connection: Connection, user: AuthUser) {
        if (user.socialAccounts.isEmpty()) {
            return
        }
        connection.prepareStatement(
            """
            INSERT INTO auth_social_accounts (provider, external_subject, user_id)
            VALUES (?, ?, ?)
            """.trimIndent(),
        ).use { statement ->
            user.socialAccounts.forEach { account ->
                statement.setString(1, account.provider.name)
                statement.setString(2, account.externalSubject)
                statement.setString(3, user.id)
                statement.addBatch()
            }
            statement.executeBatch()
        }
    }

    private data class UserRow(
        val id: String,
        val email: String?,
        val displayName: String?,
        val passwordHash: String?,
    )
}
