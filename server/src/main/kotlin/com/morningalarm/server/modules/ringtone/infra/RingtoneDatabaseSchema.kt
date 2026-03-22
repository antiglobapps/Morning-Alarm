package com.morningalarm.server.modules.ringtone.infra

import com.morningalarm.server.shared.persistence.SchemaBootstrap
import java.sql.Connection
import javax.sql.DataSource

class RingtoneDatabaseSchema(
    private val dataSource: DataSource,
) : SchemaBootstrap {
    override val name: String = "ringtone"

    override fun ensureCreated() {
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.execute(
                    """
                    CREATE TABLE IF NOT EXISTS ringtones (
                        id VARCHAR(128) PRIMARY KEY,
                        title VARCHAR(255) NOT NULL,
                        image_url TEXT NOT NULL,
                        audio_url TEXT NOT NULL,
                        duration_seconds INTEGER NOT NULL,
                        description TEXT NOT NULL,
                        visibility VARCHAR(32) NOT NULL,
                        is_premium BOOLEAN NOT NULL,
                        created_at_epoch_seconds BIGINT NOT NULL,
                        updated_at_epoch_seconds BIGINT NOT NULL,
                        created_by_admin_id VARCHAR(128) REFERENCES business_users(id),
                        updated_by_admin_id VARCHAR(128) REFERENCES business_users(id),
                        created_by_user_id VARCHAR(128) REFERENCES business_users(id)
                    )
                    """.trimIndent(),
                )
                if (!connection.hasColumn("ringtones", "visibility")) {
                    statement.execute("ALTER TABLE ringtones ADD COLUMN visibility VARCHAR(32)")
                }
                if (!connection.hasColumn("ringtones", "created_by_user_id")) {
                    statement.execute("ALTER TABLE ringtones ADD COLUMN created_by_user_id VARCHAR(128) REFERENCES business_users(id)")
                }
                if (connection.hasColumn("ringtones", "is_active")) {
                    statement.execute(
                        """
                        UPDATE ringtones
                        SET visibility = CASE
                            WHEN is_active = TRUE THEN 'PUBLIC'
                            ELSE 'INACTIVE'
                        END
                        WHERE visibility IS NULL
                        """.trimIndent(),
                    )
                }
                statement.execute("ALTER TABLE ringtones ALTER COLUMN visibility SET NOT NULL")
                statement.execute(
                    """
                    CREATE TABLE IF NOT EXISTS user_ringtone_likes (
                        user_id VARCHAR(128) NOT NULL REFERENCES business_users(id) ON DELETE CASCADE,
                        ringtone_id VARCHAR(128) NOT NULL REFERENCES ringtones(id) ON DELETE CASCADE,
                        PRIMARY KEY (user_id, ringtone_id)
                    )
                    """.trimIndent(),
                )
                statement.execute("CREATE INDEX IF NOT EXISTS idx_user_ringtone_likes_ringtone_id ON user_ringtone_likes(ringtone_id)")
                statement.execute("CREATE INDEX IF NOT EXISTS idx_ringtones_visibility ON ringtones(visibility)")
                statement.execute("CREATE INDEX IF NOT EXISTS idx_ringtones_created_by_user_id ON ringtones(created_by_user_id)")
            }
        }
    }
}

private fun Connection.hasColumn(tableName: String, columnName: String): Boolean {
    metaData.getColumns(null, null, tableName, columnName).use { resultSet ->
        return resultSet.next()
    }
}
