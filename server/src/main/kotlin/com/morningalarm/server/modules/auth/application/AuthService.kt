package com.morningalarm.server.modules.auth.application

import com.morningalarm.server.modules.auth.domain.AuthSession
import com.morningalarm.server.modules.auth.domain.SocialProvider

class AuthService(
    private val adminLoginService: AdminLoginService,
    private val socialAuthService: SocialAuthService,
    private val emailAuthService: EmailAuthService,
) {
    fun adminLogin(email: String, password: String, adminSecret: String, requiredAdminSecret: String?): AuthSession {
        return adminLoginService.adminLogin(email, password, adminSecret, requiredAdminSecret)
    }

    fun socialAuth(
        provider: SocialProvider,
        token: String,
        email: String?,
        displayName: String?,
    ): AuthSession {
        return socialAuthService.socialAuth(provider, token, email, displayName)
    }

    fun registerWithEmail(
        email: String,
        password: String,
        displayName: String?,
    ): AuthSession {
        return emailAuthService.registerWithEmail(email, password, displayName)
    }

    fun loginWithEmail(email: String, password: String): AuthSession {
        return emailAuthService.loginWithEmail(email, password)
    }

    fun requestPasswordReset(email: String) {
        emailAuthService.requestPasswordReset(email)
    }

    fun confirmPasswordReset(token: String, newPassword: String): AuthSession {
        return emailAuthService.confirmPasswordReset(token, newPassword)
    }

    fun refresh(refreshToken: String): AuthSession {
        return emailAuthService.refresh(refreshToken)
    }
}
