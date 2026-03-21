package com.morningalarm.server

import com.morningalarm.server.bootstrap.AppConfig
import com.morningalarm.server.bootstrap.ModuleDependencies
import com.morningalarm.server.bootstrap.applicationModule
import com.morningalarm.server.bootstrap.createModuleDependencies
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import java.nio.file.Files

fun testConfig(dataDir: String): AppConfig = AppConfig(
    devMode = true,
    host = "127.0.0.1",
    port = 8080,
    publicUrl = null,
    logPublicUrl = false,
    mediaStorageDir = "$dataDir/media",
    mediaPublicBaseUrl = "http://localhost:8080",
    mediaMaxImageBytes = 1024 * 1024,
    mediaMaxAudioBytes = 5 * 1024 * 1024,
    firebaseBucketName = null,
    firebaseCredentialsPath = null,
    databaseUrl = "jdbc:h2:file:$dataDir/test-db;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
    databaseUser = "sa",
    databasePassword = "",
    databaseDriver = "org.h2.Driver",
    databasePoolMaxSize = 4,
    jwtSecret = "test-secret",
    jwtIssuer = "test-issuer",
    jwtAudience = "test-audience",
    adminEmails = setOf("admin@example.com"),
    adminBootstrapSecret = null,
    adminAccessSecret = null,
    accessTokenTtlSeconds = 24 * 60 * 60L,
    refreshTokenTtlSeconds = 30 * 24 * 60 * 60L,
    passwordResetTokenTtlSeconds = 60 * 60L,
    adminLoginMaxAttempts = 1000,
    adminLoginWindowSeconds = 300L,
)

fun testApp(
    configOverride: (AppConfig.() -> AppConfig) = { this },
    block: suspend ApplicationTestBuilder.(ModuleDependencies, HttpClient) -> Unit,
) = testApplication {
    val config = configOverride(testConfig(Files.createTempDirectory("morning-alarm-test").toString()))
    val dependencies = createModuleDependencies(config)
    application {
        applicationModule(config = config, dependencies = dependencies)
    }
    val client = createClient {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = false
                    explicitNulls = false
                },
            )
        }
    }
    block(dependencies, client)
}
