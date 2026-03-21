package com.morningalarm.server.shared.tracing

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.callid.CallId
import java.util.UUID

fun Application.configureRequestId(headerName: String) {
    install(CallId) {
        header(headerName)
        generate {
            UUID.randomUUID().toString()
        }
        verify { it.isNotBlank() }
        replyToHeader(headerName)
    }
}
