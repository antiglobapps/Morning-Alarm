package com.morningalarm.server.modules.user.infra

import com.morningalarm.server.modules.auth.application.ports.BusinessUserRepository
import com.morningalarm.server.modules.auth.domain.UserRole
import com.morningalarm.server.modules.user.domain.BusinessUser
import com.morningalarm.server.shared.persistence.JdbcSessionManager

class PostgresBusinessUserRepository(
    private val sessionManager: JdbcSessionManager,
) : BusinessUserRepository {
    override fun findById(id: String): BusinessUser? {
        return sessionManager.withConnection { connection ->
            connection.prepareStatement(
                """
                SELECT id, email, display_name, role
                FROM business_users
                WHERE id = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, id)
                statement.executeQuery().use { resultSet ->
                    if (!resultSet.next()) {
                        null
                    } else {
                        BusinessUser(
                            id = resultSet.getString("id"),
                            email = resultSet.getString("email"),
                            displayName = resultSet.getString("display_name"),
                            role = UserRole.valueOf(resultSet.getString("role")),
                        )
                    }
                }
            }
        }
    }

    override fun findByEmail(email: String): BusinessUser? {
        return sessionManager.withConnection { connection ->
            connection.prepareStatement(
                """
                SELECT id, email, display_name, role
                FROM business_users
                WHERE email = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, email)
                statement.executeQuery().use { resultSet ->
                    if (!resultSet.next()) null
                    else BusinessUser(
                        id = resultSet.getString("id"),
                        email = resultSet.getString("email"),
                        displayName = resultSet.getString("display_name"),
                        role = UserRole.valueOf(resultSet.getString("role")),
                    )
                }
            }
        }
    }

    override fun updateRole(userId: String, role: UserRole) {
        sessionManager.withConnection { connection ->
            connection.prepareStatement(
                """
                UPDATE business_users SET role = ? WHERE id = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, role.name)
                statement.setString(2, userId)
                statement.executeUpdate()
            }
        }
    }

    override fun ensureUser(id: String, email: String?, displayName: String?, role: UserRole): BusinessUser {
        return sessionManager.inTransaction {
            val existing = findById(id)
            if (existing == null) {
                sessionManager.withConnection { connection ->
                    connection.prepareStatement(
                        """
                        INSERT INTO business_users (id, email, display_name, role)
                        VALUES (?, ?, ?, ?)
                        """.trimIndent(),
                    ).use { statement ->
                        statement.setString(1, id)
                        statement.setString(2, email)
                        statement.setString(3, displayName)
                        statement.setString(4, role.name)
                        statement.executeUpdate()
                    }
                }
                return@inTransaction BusinessUser(id, email, displayName, role)
            }

            if (existing.email != email || existing.displayName != displayName || existing.role != role) {
                sessionManager.withConnection { connection ->
                    connection.prepareStatement(
                        """
                        UPDATE business_users
                        SET email = ?, display_name = ?, role = ?
                        WHERE id = ?
                        """.trimIndent(),
                    ).use { statement ->
                        statement.setString(1, email)
                        statement.setString(2, displayName)
                        statement.setString(3, role.name)
                        statement.setString(4, id)
                        statement.executeUpdate()
                    }
                }
                return@inTransaction BusinessUser(id, email, displayName, role)
            }

            existing
        }
    }
}
