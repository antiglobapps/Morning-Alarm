package com.morningalarm.server.shared.health

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.configureHealthRoutes() {
    get("/health/live") {
        call.respondText("OK", status = HttpStatusCode.OK)
    }
    get("/health/ready") {
        call.respondText("OK", status = HttpStatusCode.OK)
    }
}
