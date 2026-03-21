package com.morningalarm.server.modules.ringtone.infra

import javax.sql.DataSource

class RingtoneDatabaseSchema(
    private val dataSource: DataSource,
) {
    fun ensureCreated() {
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
                        is_active BOOLEAN NOT NULL,
                        is_premium BOOLEAN NOT NULL,
                        created_at_epoch_seconds BIGINT NOT NULL,
                        updated_at_epoch_seconds BIGINT NOT NULL,
                        created_by_admin_id VARCHAR(128) NOT NULL REFERENCES business_users(id),
                        updated_by_admin_id VARCHAR(128) NOT NULL REFERENCES business_users(id)
                    )
                    """.trimIndent(),
                )
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
                statement.execute("CREATE INDEX IF NOT EXISTS idx_ringtones_is_active ON ringtones(is_active)")
            }
        }
    }
}
