package com.morningalarm.server.modules.ringtone.infra

import com.morningalarm.server.modules.ringtone.application.ports.RingtoneRepository
import com.morningalarm.server.modules.ringtone.domain.Ringtone
import com.morningalarm.server.modules.ringtone.domain.RingtoneLikeToggleResult
import com.morningalarm.server.modules.ringtone.domain.RingtoneView
import javax.sql.DataSource

class PostgresRingtoneRepository(
    private val dataSource: DataSource,
) : RingtoneRepository {
    override fun listForUser(userId: String): List<RingtoneView> {
        return dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT
                    r.id, r.title, r.image_url, r.audio_url, r.duration_seconds, r.description,
                    r.is_active, r.is_premium, r.created_at_epoch_seconds, r.updated_at_epoch_seconds,
                    r.created_by_admin_id, r.updated_by_admin_id,
                    COUNT(ul.user_id) AS likes_count,
                    EXISTS (
                        SELECT 1 FROM user_ringtone_likes my_like
                        WHERE my_like.ringtone_id = r.id AND my_like.user_id = ?
                    ) AS is_liked_by_user
                FROM ringtones r
                LEFT JOIN user_ringtone_likes ul ON ul.ringtone_id = r.id
                WHERE r.is_active = TRUE
                GROUP BY r.id, r.title, r.image_url, r.audio_url, r.duration_seconds, r.description,
                    r.is_active, r.is_premium, r.created_at_epoch_seconds, r.updated_at_epoch_seconds,
                    r.created_by_admin_id, r.updated_by_admin_id
                ORDER BY r.title ASC
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, userId)
                statement.executeQuery().use { resultSet ->
                    buildList {
                        while (resultSet.next()) add(resultSet.toRingtoneView())
                    }
                }
            }
        }
    }

    override fun findForUser(userId: String, ringtoneId: String): RingtoneView? {
        return dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT
                    r.id, r.title, r.image_url, r.audio_url, r.duration_seconds, r.description,
                    r.is_active, r.is_premium, r.created_at_epoch_seconds, r.updated_at_epoch_seconds,
                    r.created_by_admin_id, r.updated_by_admin_id,
                    COUNT(ul.user_id) AS likes_count,
                    EXISTS (
                        SELECT 1 FROM user_ringtone_likes my_like
                        WHERE my_like.ringtone_id = r.id AND my_like.user_id = ?
                    ) AS is_liked_by_user
                FROM ringtones r
                LEFT JOIN user_ringtone_likes ul ON ul.ringtone_id = r.id
                WHERE r.id = ? AND r.is_active = TRUE
                GROUP BY r.id, r.title, r.image_url, r.audio_url, r.duration_seconds, r.description,
                    r.is_active, r.is_premium, r.created_at_epoch_seconds, r.updated_at_epoch_seconds,
                    r.created_by_admin_id, r.updated_by_admin_id
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, userId)
                statement.setString(2, ringtoneId)
                statement.executeQuery().use { resultSet ->
                    if (!resultSet.next()) null else resultSet.toRingtoneView()
                }
            }
        }
    }

    override fun listForAdmin(): List<RingtoneView> {
        return dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT
                    r.id, r.title, r.image_url, r.audio_url, r.duration_seconds, r.description,
                    r.is_active, r.is_premium, r.created_at_epoch_seconds, r.updated_at_epoch_seconds,
                    r.created_by_admin_id, r.updated_by_admin_id,
                    COUNT(ul.user_id) AS likes_count,
                    FALSE AS is_liked_by_user
                FROM ringtones r
                LEFT JOIN user_ringtone_likes ul ON ul.ringtone_id = r.id
                GROUP BY r.id, r.title, r.image_url, r.audio_url, r.duration_seconds, r.description,
                    r.is_active, r.is_premium, r.created_at_epoch_seconds, r.updated_at_epoch_seconds,
                    r.created_by_admin_id, r.updated_by_admin_id
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
        return dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                SELECT
                    r.id, r.title, r.image_url, r.audio_url, r.duration_seconds, r.description,
                    r.is_active, r.is_premium, r.created_at_epoch_seconds, r.updated_at_epoch_seconds,
                    r.created_by_admin_id, r.updated_by_admin_id,
                    COUNT(ul.user_id) AS likes_count,
                    FALSE AS is_liked_by_user
                FROM ringtones r
                LEFT JOIN user_ringtone_likes ul ON ul.ringtone_id = r.id
                WHERE r.id = ?
                GROUP BY r.id, r.title, r.image_url, r.audio_url, r.duration_seconds, r.description,
                    r.is_active, r.is_premium, r.created_at_epoch_seconds, r.updated_at_epoch_seconds,
                    r.created_by_admin_id, r.updated_by_admin_id
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
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                INSERT INTO ringtones (
                    id, title, image_url, audio_url, duration_seconds, description,
                    is_active, is_premium, created_at_epoch_seconds, updated_at_epoch_seconds,
                    created_by_admin_id, updated_by_admin_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, ringtone.id)
                statement.setString(2, ringtone.title)
                statement.setString(3, ringtone.imageUrl)
                statement.setString(4, ringtone.audioUrl)
                statement.setInt(5, ringtone.durationSeconds)
                statement.setString(6, ringtone.description)
                statement.setBoolean(7, ringtone.isActive)
                statement.setBoolean(8, ringtone.isPremium)
                statement.setLong(9, ringtone.createdAtEpochSeconds)
                statement.setLong(10, ringtone.updatedAtEpochSeconds)
                statement.setString(11, ringtone.createdByAdminId)
                statement.setString(12, ringtone.updatedByAdminId)
                statement.executeUpdate()
            }
        }
        return ringtone
    }

    override fun update(ringtone: Ringtone): Ringtone? {
        val updated = dataSource.connection.use { connection ->
            connection.prepareStatement(
                """
                UPDATE ringtones
                SET title = ?, image_url = ?, audio_url = ?, duration_seconds = ?, description = ?,
                    is_active = ?, is_premium = ?, updated_at_epoch_seconds = ?, updated_by_admin_id = ?
                WHERE id = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, ringtone.title)
                statement.setString(2, ringtone.imageUrl)
                statement.setString(3, ringtone.audioUrl)
                statement.setInt(4, ringtone.durationSeconds)
                statement.setString(5, ringtone.description)
                statement.setBoolean(6, ringtone.isActive)
                statement.setBoolean(7, ringtone.isPremium)
                statement.setLong(8, ringtone.updatedAtEpochSeconds)
                statement.setString(9, ringtone.updatedByAdminId)
                statement.setString(10, ringtone.id)
                statement.executeUpdate()
            }
        }
        if (updated == 0) return null
        return ringtone
    }

    override fun delete(ringtoneId: String): Boolean {
        return dataSource.connection.use { connection ->
            connection.prepareStatement("DELETE FROM ringtones WHERE id = ?").use { statement ->
                statement.setString(1, ringtoneId)
                statement.executeUpdate() > 0
            }
        }
    }

    override fun toggleLike(userId: String, ringtoneId: String): RingtoneLikeToggleResult? {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            try {
                val exists = connection.prepareStatement("SELECT 1 FROM ringtones WHERE id = ? AND is_active = TRUE").use { statement ->
                    statement.setString(1, ringtoneId)
                    statement.executeQuery().use { it.next() }
                }
                if (!exists) {
                    connection.rollback()
                    return null
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
                connection.commit()
                return RingtoneLikeToggleResult(ringtoneId, !liked, likesCount)
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                connection.autoCommit = true
            }
        }
    }

    private fun countLikes(ringtoneId: String): Int {
        return dataSource.connection.use { connection ->
            connection.prepareStatement("SELECT COUNT(*) FROM user_ringtone_likes WHERE ringtone_id = ?").use { statement ->
                statement.setString(1, ringtoneId)
                statement.executeQuery().use {
                    it.next()
                    it.getInt(1)
                }
            }
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
        isActive = getBoolean("is_active"),
        isPremium = getBoolean("is_premium"),
        createdAtEpochSeconds = getLong("created_at_epoch_seconds"),
        updatedAtEpochSeconds = getLong("updated_at_epoch_seconds"),
        createdByAdminId = getString("created_by_admin_id"),
        updatedByAdminId = getString("updated_by_admin_id"),
    ),
    likesCount = getInt("likes_count"),
    isLikedByUser = getBoolean("is_liked_by_user"),
)
