package com.morningalarm.server.bootstrap

import com.morningalarm.api.ApiHeaders
import com.morningalarm.server.shared.auth.configureAuthentication
import com.morningalarm.server.shared.errors.configureStatusPages
import com.morningalarm.server.shared.health.configureHealthRoutes
import com.morningalarm.server.shared.docs.configureOpenApiRoutes
import com.morningalarm.server.shared.tracing.configureRequestId
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

fun Application.applicationModule(
    config: AppConfig = AppConfigLoader.fromEnv(),
    dependencies: ModuleDependencies = createModuleDependencies(config),
) {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = false
                isLenient = false
                ignoreUnknownKeys = false
                explicitNulls = false
            },
        )
    }
    install(Compression)
    install(CallLogging)

    configureAuthentication(dependencies.accessTokenService)
    configureRequestId(ApiHeaders.REQUEST_ID)
    configureStatusPages()

    routing {
        configureHealthRoutes()
        configureOpenApiRoutes()
        configureRouting(dependencies)
    }
}
