package com.morningalarm.server.shared.docs

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

private const val OPEN_API_RESOURCE = "openapi/documentation.yaml"

fun Route.configureOpenApiRoutes() {
    openAPI(path = "openapi", swaggerFile = OPEN_API_RESOURCE)
    swaggerUI(path = "swagger", swaggerFile = OPEN_API_RESOURCE)

    get("/openapi.yaml") {
        val specification = object {}::class.java.classLoader
            .getResource(OPEN_API_RESOURCE)
            ?.readText()
            ?: error("OpenAPI specification resource '$OPEN_API_RESOURCE' was not found")

        call.respondText(
            text = specification,
            contentType = ContentType.parse("application/yaml"),
            status = HttpStatusCode.OK,
        )
    }
}
