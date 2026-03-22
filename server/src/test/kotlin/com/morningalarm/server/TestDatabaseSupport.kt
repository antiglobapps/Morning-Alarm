package com.morningalarm.server

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.nio.file.Files

fun createTestDataSource(dataDir: String): HikariDataSource {
    val hikariConfig = HikariConfig().apply {
        jdbcUrl = "jdbc:h2:file:$dataDir/test-db;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
        username = "sa"
        password = ""
        driverClassName = "org.h2.Driver"
        maximumPoolSize = 2
        minimumIdle = 1
        isAutoCommit = true
        validate()
    }
    return HikariDataSource(hikariConfig)
}

fun createTestDataDir(prefix: String): String = Files.createTempDirectory(prefix).toString()
