package com.morningalarm.server.shared.errors

import com.morningalarm.api.ApiHeaders
import com.morningalarm.dto.ApiError
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<ValidationException> { call, cause ->
            call.respondError(HttpStatusCode.BadRequest, cause)
        }
        exception<NotFoundException> { call, cause ->
            call.respondError(HttpStatusCode.NotFound, cause)
        }
        exception<ConflictException> { call, cause ->
            call.respondError(HttpStatusCode.Conflict, cause)
        }
        exception<UnauthorizedException> { call, cause ->
            call.respondError(HttpStatusCode.Unauthorized, cause)
        }
        exception<ForbiddenException> { call, cause ->
            call.respondError(HttpStatusCode.Forbidden, cause)
        }
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiError(
                    code = "unexpected_error",
                    message = "Unexpected server error",
                    details = cause.message,
                    requestId = call.response.headers[ApiHeaders.REQUEST_ID],
                ),
            )
        }
    }
}

private suspend fun io.ktor.server.application.ApplicationCall.respondError(
    status: HttpStatusCode,
    cause: AppException,
) {
    respond(
        status,
        ApiError(
            code = cause.code,
            message = cause.message,
            details = cause.details,
            requestId = response.headers[ApiHeaders.REQUEST_ID],
        ),
    )
}
