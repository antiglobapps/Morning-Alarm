package com.morningalarm.server.shared.auth

import com.morningalarm.api.ApiHeaders
import com.morningalarm.server.modules.auth.application.ports.AccessTokenService
import com.morningalarm.server.modules.auth.domain.UserRole
import com.morningalarm.server.shared.errors.ForbiddenException
import com.morningalarm.server.shared.errors.UnauthorizedException
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.Principal
import io.ktor.server.auth.bearer
import io.ktor.server.auth.principal
import io.ktor.server.request.header

const val ADMIN_SECRET_HEADER = ApiHeaders.ADMIN_SECRET

data class AuthPrincipal(
    val userId: String,
    val role: UserRole,
) : Principal

fun Application.configureAuthentication(accessTokenService: AccessTokenService) {
    install(Authentication) {
        bearer("auth-bearer") {
            authenticate { credential ->
                val payload = accessTokenService.verify(credential.token) ?: return@authenticate null
                AuthPrincipal(payload.userId, payload.role)
            }
        }
    }
}

fun ApplicationCall.currentAuthPrincipal(): AuthPrincipal {
    return principal<AuthPrincipal>() ?: throw UnauthorizedException("Authorization is required")
}

/**
 * Requires ADMIN role AND valid X-Admin-Secret header (when configured).
 * If SERVER_ADMIN_ACCESS_SECRET is not set, only role check is performed (dev mode).
 */
fun ApplicationCall.requireAdmin(adminAccessSecret: String? = null) {
    val principal = currentAuthPrincipal()
    if (principal.role != UserRole.ADMIN) {
        throw ForbiddenException("Admin role is required")
    }
    if (!adminAccessSecret.isNullOrBlank()) {
        val headerSecret = request.header(ADMIN_SECRET_HEADER)
        if (headerSecret != adminAccessSecret) {
            throw ForbiddenException("Invalid or missing $ADMIN_SECRET_HEADER header")
        }
    }
}
