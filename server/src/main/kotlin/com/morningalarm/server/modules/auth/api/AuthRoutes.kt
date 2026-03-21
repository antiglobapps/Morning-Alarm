package com.morningalarm.server.modules.auth.api

import com.morningalarm.api.auth.AuthRoutes
import com.morningalarm.dto.auth.AdminLoginRequestDto
import com.morningalarm.dto.auth.AdminLoginResponseDto
import com.morningalarm.dto.auth.EmailLoginRequestDto
import com.morningalarm.dto.auth.EmailLoginResponseDto
import com.morningalarm.dto.auth.EmailRegisterRequestDto
import com.morningalarm.dto.auth.EmailRegisterResponseDto
import com.morningalarm.dto.auth.PasswordResetConfirmRequestDto
import com.morningalarm.dto.auth.PasswordResetConfirmResponseDto
import com.morningalarm.dto.auth.PasswordResetRequestDto
import com.morningalarm.dto.auth.PasswordResetRequestResponseDto
import com.morningalarm.dto.auth.RefreshTokenRequestDto
import com.morningalarm.dto.auth.RefreshTokenResponseDto
import com.morningalarm.dto.auth.SocialAuthRequestDto
import com.morningalarm.dto.auth.SocialAuthResponseDto
import com.morningalarm.server.modules.auth.application.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.configureAuthRoutes(authService: AuthService, adminAccessSecret: String? = null) {
    post(AuthRoutes.SOCIAL) {
        val request = call.receive<SocialAuthRequestDto>()
        val session = authService.socialAuth(
            provider = request.provider.toDomain(),
            token = request.token,
            email = request.email,
            displayName = request.displayName,
        )
        call.respond(HttpStatusCode.OK, SocialAuthResponseDto(session.toDto()))
    }

    post(AuthRoutes.EMAIL_REGISTER) {
        val request = call.receive<EmailRegisterRequestDto>()
        val session = authService.registerWithEmail(
            email = request.email,
            password = request.password,
            displayName = request.displayName,
        )
        call.respond(HttpStatusCode.Created, EmailRegisterResponseDto(session.toDto()))
    }

    post(AuthRoutes.EMAIL_LOGIN) {
        val request = call.receive<EmailLoginRequestDto>()
        val session = authService.loginWithEmail(
            email = request.email,
            password = request.password,
        )
        call.respond(HttpStatusCode.OK, EmailLoginResponseDto(session.toDto()))
    }

    post(AuthRoutes.PASSWORD_RESET_REQUEST) {
        val request = call.receive<PasswordResetRequestDto>()
        authService.requestPasswordReset(request.email)
        call.respond(
            HttpStatusCode.OK,
            PasswordResetRequestResponseDto(
                email = request.email.trim().lowercase(),
                resetRequested = true,
            ),
        )
    }

    post(AuthRoutes.PASSWORD_RESET_CONFIRM) {
        val request = call.receive<PasswordResetConfirmRequestDto>()
        val session = authService.confirmPasswordReset(
            token = request.token,
            newPassword = request.newPassword,
        )
        call.respond(HttpStatusCode.OK, PasswordResetConfirmResponseDto(session.toDto()))
    }

    post(AuthRoutes.TOKEN_REFRESH) {
        val request = call.receive<RefreshTokenRequestDto>()
        val session = authService.refresh(request.refreshToken)
        call.respond(HttpStatusCode.OK, RefreshTokenResponseDto(session.toDto()))
    }

    post(AuthRoutes.ADMIN_LOGIN) {
        val request = call.receive<AdminLoginRequestDto>()
        val session = authService.adminLogin(
            email = request.email,
            password = request.password,
            adminSecret = request.adminSecret,
            requiredAdminSecret = adminAccessSecret,
        )
        call.respond(HttpStatusCode.OK, AdminLoginResponseDto(session.toDto()))
    }
}
