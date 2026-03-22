package com.morningalarm.server.modules.ringtone.infra

import com.morningalarm.server.modules.ringtone.application.ports.RingtoneRepository
import com.morningalarm.server.modules.ringtone.domain.Ringtone
import com.morningalarm.server.modules.ringtone.domain.RingtoneListFilter
import com.morningalarm.server.modules.ringtone.domain.RingtoneLikeToggleResult
import com.morningalarm.server.modules.ringtone.domain.RingtoneView
import com.morningalarm.server.modules.ringtone.domain.RingtoneVisibility
import com.morningalarm.server.shared.persistence.JdbcSessionManager

private const val SELECT_COLUMNS = """
    r.id, r.title, r.image_url, r.audio_url, r.duration_seconds, r.description,
    r.visibility, r.is_premium, r.created_at_epoch_seconds, r.updated_at_epoch_seconds,
    r.created_by_admin_id, r.updated_by_admin_id, r.created_by_user_id
"""

private const val GROUP_BY_COLUMNS = """
    r.id, r.title, r.image_url, r.audio_url, r.duration_seconds, r.description,
    r.visibility, r.is_premium, r.created_at_epoch_seconds, r.updated_at_epoch_seconds,
    r.created_by_admin_id, r.updated_by_admin_id, r.created_by_user_id
"""

class PostgresRingtoneRepository(
    private val sessionManager: JdbcSessionManager,
) : RingtoneRepository {
    override fun listForUser(userId: String, filter: RingtoneListFilter): List<RingtoneView> {
        val whereClause = when (filter) {
            RingtoneListFilter.ALL ->
                "WHERE (r.visibility = 'PUBLIC') OR (r.visibility = 'PRIVATE' AND r.created_by_user_id = ?)"
            RingtoneListFilter.MY ->
                "WHERE r.created_by_user_id = ? AND r.visibility != 'INACTIVE'"
            RingtoneListFilter.SYSTEM ->
                "WHERE r.visibility = 'PUBLIC' AND r.created_by_user_id IS NULL"
        }

        return sessionManager.withConnection { connection ->
            connection.prepareStatement(
                """
                SELECT
                    $SELECT_COLUMNS,
                    COUNT(ul.user_id) AS likes_count,
                    EXISTS (
                        SELECT 1 FROM user_ringtone_likes my_like
                        WHERE my_like.ringtone_id = r.id AND my_like.user_id = ?
                    ) AS is_liked_by_user
                FROM ringtones r
                LEFT JOIN user_ringtone_likes ul ON ul.ringtone_id = r.id
                $whereClause
                GROUP BY $GROUP_BY_COLUMNS
                ORDER BY r.title ASC
                """.trimIndent(),
            ).use { statement ->
                var paramIndex = 1
                statement.setString(paramIndex++, userId) // for is_liked_by_user subquery
                when (filter) {
                    RingtoneListFilter.ALL -> statement.setString(paramIndex++, userId)
                    RingtoneListFilter.MY -> statement.setString(paramIndex++, userId)
                    RingtoneListFilter.SYSTEM -> { /* no extra param */ }
                }
                statement.executeQuery().use { resultSet ->
                    buildList {
                        while (resultSet.next()) add(resultSet.toRingtoneView())
                    }
                }
            }
        }
    }

    override fun findForUser(userId: String, ringtoneId: String): RingtoneView? {
        return sessionManager.withConnection { connection ->
            connection.prepareStatement(
                """
                SELECT
                    $SELECT_COLUMNS,
                    COUNT(ul.user_id) AS likes_count,
                    EXISTS (
                        SELECT 1 FROM user_ringtone_likes my_like
                        WHERE my_like.ringtone_id = r.id AND my_like.user_id = ?
                    ) AS is_liked_by_user
                FROM ringtones r
                LEFT JOIN user_ringtone_likes ul ON ul.ringtone_id = r.id
                WHERE r.id = ? AND ((r.visibility = 'PUBLIC') OR (r.visibility = 'PRIVATE' AND r.created_by_user_id = ?))
                GROUP BY $GROUP_BY_COLUMNS
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, userId)
                statement.setString(2, ringtoneId)
                statement.setString(3, userId)
                statement.executeQuery().use { resultSet ->
                    if (!resultSet.next()) null else resultSet.toRingtoneView()
                }
            }
        }
    }

    override fun listForAdmin(): List<RingtoneView> {
        return sessionManager.withConnection { connection ->
            connection.prepareStatement(
                """
                SELECT
                    $SELECT_COLUMNS,
                    COUNT(ul.user_id) AS likes_count,
                    FALSE AS is_liked_by_user
                FROM ringtones r
                LEFT JOIN user_ringtone_likes ul ON ul.ringtone_id = r.id
                GROUP BY $GROUP_BY_COLUMNS
                ORDER BY r.updated_at_epoch_seconds DESC, r.title ASC
                """.trimIndent(),
            ).use { statement ->
                statement.executeQuery().use { resultSet ->
                    buildList {
                        while (resultSet.next()) add(resultSet.toRingtoneView())
                    }
                }
            }
        }
    }

    override fun findForAdmin(ringtoneId: String): RingtoneView? {
        return sessionManager.withConnection { connection ->
            connection.prepareStatement(
                """
                SELECT
                    $SELECT_COLUMNS,
                    COUNT(ul.user_id) AS likes_count,
                    FALSE AS is_liked_by_user
                FROM ringtones r
                LEFT JOIN user_ringtone_likes ul ON ul.ringtone_id = r.id
                WHERE r.id = ?
                GROUP BY $GROUP_BY_COLUMNS
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, ringtoneId)
                statement.executeQuery().use { resultSet ->
                    if (!resultSet.next()) null else resultSet.toRingtoneView()
                }
            }
        }
    }

    override fun create(ringtone: Ringtone): Ringtone {
        sessionManager.withConnection { connection ->
            connection.prepareStatement(
                """
                INSERT INTO ringtones (
                    id, title, image_url, audio_url, duration_seconds, description,
                    visibility, is_premium, created_at_epoch_seconds, updated_at_epoch_seconds,
                    created_by_admin_id, updated_by_admin_id, created_by_user_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, ringtone.id)
                statement.setString(2, ringtone.title)
                statement.setString(3, ringtone.imageUrl)
                statement.setString(4, ringtone.audioUrl)
                statement.setInt(5, ringtone.durationSeconds)
                statement.setString(6, ringtone.description)
                statement.setString(7, ringtone.visibility.name)
                statement.setBoolean(8, ringtone.isPremium)
                statement.setLong(9, ringtone.createdAtEpochSeconds)
                statement.setLong(10, ringtone.updatedAtEpochSeconds)
                setNullableString(statement, 11, ringtone.createdByAdminId)
                setNullableString(statement, 12, ringtone.updatedByAdminId)
                setNullableString(statement, 13, ringtone.createdByUserId)
                statement.executeUpdate()
            }
        }
        return ringtone
    }

    override fun update(ringtone: Ringtone): Ringtone? {
        val updated = sessionManager.withConnection { connection ->
            connection.prepareStatement(
                """
                UPDATE ringtones
                SET title = ?, image_url = ?, audio_url = ?, duration_seconds = ?, description = ?,
                    visibility = ?, is_premium = ?, updated_at_epoch_seconds = ?, updated_by_admin_id = ?
                WHERE id = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, ringtone.title)
                statement.setString(2, ringtone.imageUrl)
                statement.setString(3, ringtone.audioUrl)
                statement.setInt(4, ringtone.durationSeconds)
                statement.setString(5, ringtone.description)
                statement.setString(6, ringtone.visibility.name)
                statement.setBoolean(7, ringtone.isPremium)
                statement.setLong(8, ringtone.updatedAtEpochSeconds)
                setNullableString(statement, 9, ringtone.updatedByAdminId)
                statement.setString(10, ringtone.id)
                statement.executeUpdate()
            }
        }
        if (updated == 0) return null
        return ringtone
    }

    override fun delete(ringtoneId: String): Boolean {
        return sessionManager.withConnection { connection ->
            connection.prepareStatement("DELETE FROM ringtones WHERE id = ?").use { statement ->
                statement.setString(1, ringtoneId)
                statement.executeUpdate() > 0
            }
        }
    }

    override fun toggleLike(userId: String, ringtoneId: String): RingtoneLikeToggleResult? {
        return sessionManager.inTransaction {
            sessionManager.withConnection { connection ->
                val exists = connection.prepareStatement(
                    """
                    SELECT 1 FROM ringtones
                    WHERE id = ? AND ((visibility = 'PUBLIC') OR (visibility = 'PRIVATE' AND created_by_user_id = ?))
                    """.trimIndent(),
                ).use { statement ->
                    statement.setString(1, ringtoneId)
                    statement.setString(2, userId)
                    statement.executeQuery().use { it.next() }
                }
                if (!exists) {
                    return@withConnection null
                }

                val liked = connection.prepareStatement(
                    "SELECT 1 FROM user_ringtone_likes WHERE user_id = ? AND ringtone_id = ?",
                ).use { statement ->
                    statement.setString(1, userId)
                    statement.setString(2, ringtoneId)
                    statement.executeQuery().use { it.next() }
                }

                if (liked) {
                    connection.prepareStatement(
                        "DELETE FROM user_ringtone_likes WHERE user_id = ? AND ringtone_id = ?",
                    ).use { statement ->
                        statement.setString(1, userId)
                        statement.setString(2, ringtoneId)
                        statement.executeUpdate()
                    }
                } else {
                    connection.prepareStatement(
                        "INSERT INTO user_ringtone_likes (user_id, ringtone_id) VALUES (?, ?)",
                    ).use { statement ->
                        statement.setString(1, userId)
                        statement.setString(2, ringtoneId)
                        statement.executeUpdate()
                    }
                }

                val likesCount = connection.prepareStatement(
                    "SELECT COUNT(*) FROM user_ringtone_likes WHERE ringtone_id = ?",
                ).use { statement ->
                    statement.setString(1, ringtoneId)
                    statement.executeQuery().use {
                        it.next()
                        it.getInt(1)
                    }
                }
                RingtoneLikeToggleResult(ringtoneId, !liked, likesCount)
            }
        }
    }

    private fun setNullableString(statement: java.sql.PreparedStatement, index: Int, value: String?) {
        if (value != null) {
            statement.setString(index, value)
        } else {
            statement.setNull(index, java.sql.Types.VARCHAR)
        }
    }
}

private fun java.sql.ResultSet.toRingtoneView(): RingtoneView = RingtoneView(
    ringtone = Ringtone(
        id = getString("id"),
        title = getString("title"),
        imageUrl = getString("image_url"),
        audioUrl = getString("audio_url"),
        durationSeconds = getInt("duration_seconds"),
        description = getString("description"),
        visibility = RingtoneVisibility.valueOf(getString("visibility")),
        isPremium = getBoolean("is_premium"),
        createdAtEpochSeconds = getLong("created_at_epoch_seconds"),
        updatedAtEpochSeconds = getLong("updated_at_epoch_seconds"),
        createdByAdminId = getString("created_by_admin_id"),
        updatedByAdminId = getString("updated_by_admin_id"),
        createdByUserId = getString("created_by_user_id"),
    ),
    likesCount = getInt("likes_count"),
    isLikedByUser = getBoolean("is_liked_by_user"),
)
