package com.morningalarm.server.shared.persistence

import java.sql.Connection
import javax.sql.DataSource

class JdbcSessionManager(
    private val dataSource: DataSource,
) : TransactionRunner {
    private val currentConnection = ThreadLocal<Connection?>()

    override fun <T> inTransaction(block: () -> T): T {
        val existingConnection = currentConnection.get()
        if (existingConnection != null) {
            return block()
        }

        dataSource.connection.use { connection ->
            val previousAutoCommit = connection.autoCommit
            connection.autoCommit = false
            currentConnection.set(connection)
            try {
                val result = block()
                connection.commit()
                return result
            } catch (error: Throwable) {
                connection.rollback()
                throw error
            } finally {
                currentConnection.remove()
                connection.autoCommit = previousAutoCommit
            }
        }
    }

    fun <T> withConnection(block: (Connection) -> T): T {
        val existingConnection = currentConnection.get()
        return if (existingConnection != null) {
            block(existingConnection)
        } else {
            dataSource.connection.use(block)
        }
    }
}
