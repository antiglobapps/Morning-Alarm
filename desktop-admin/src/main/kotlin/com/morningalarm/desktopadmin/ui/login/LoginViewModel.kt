package com.morningalarm.desktopadmin.ui.login

import androidx.lifecycle.ViewModel
import com.morningalarm.desktopadmin.config.AppPreferences
import com.morningalarm.desktopadmin.config.ConnectionMode
import com.morningalarm.desktopadmin.data.AdminApiClient
import com.morningalarm.desktopadmin.ui.AdminSession
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

internal data class LoginState(
    val connectionMode: ConnectionMode = ConnectionMode.DEV,
    val customBaseUrl: String = "",
    val email: String = "",
    val password: String = "",
    val adminSecret: String = "",
    val errorMessage: String? = null,
    val inProgress: Boolean = false,
)

internal sealed interface LoginSideEffect {
    data class LoginSuccess(val session: AdminSession) : LoginSideEffect
}

internal class LoginViewModel(
    private val preferences: AppPreferences,
    private val apiClientFactory: (baseUrl: String) -> AdminApiClient = { AdminApiClient(it) },
) : ViewModel(), ContainerHost<LoginState, LoginSideEffect> {

    override val container = container<LoginState, LoginSideEffect>(
        LoginState(
            connectionMode = preferences.connectionMode,
            customBaseUrl = preferences.customBaseUrl,
            email = com.morningalarm.api.auth.DevAdminDefaults.EMAIL,
            password = com.morningalarm.api.auth.DevAdminDefaults.PASSWORD,
            adminSecret = com.morningalarm.api.auth.DevAdminDefaults.ACCESS_SECRET,
        ),
    )

    fun updateConnectionMode(mode: ConnectionMode) = intent {
        reduce { state.copy(connectionMode = mode) }
    }

    fun updateCustomBaseUrl(url: String) = intent {
        reduce { state.copy(customBaseUrl = url) }
    }

    fun updateEmail(email: String) = intent {
        reduce { state.copy(email = email) }
    }

    fun updatePassword(password: String) = intent {
        reduce { state.copy(password = password) }
    }

    fun updateAdminSecret(secret: String) = intent {
        reduce { state.copy(adminSecret = secret) }
    }

    fun login() = intent {
        reduce { state.copy(errorMessage = null, inProgress = true) }

        preferences.connectionMode = state.connectionMode
        val baseUrl = when (state.connectionMode) {
            ConnectionMode.DEV -> AppPreferences.DEV_BASE_URL
            ConnectionMode.CUSTOM -> {
                preferences.customBaseUrl = state.customBaseUrl
                state.customBaseUrl.trim()
            }
        }

        var client: AdminApiClient? = null
        try {
            client = apiClientFactory(baseUrl)
            val authSession = client.adminLogin(
                state.email.trim(),
                state.password,
                state.adminSecret.trim(),
            )
            val session = AdminSession(
                baseUrl = baseUrl,
                adminSecret = state.adminSecret.trim(),
                authSession = authSession,
            )
            postSideEffect(LoginSideEffect.LoginSuccess(session))
        } catch (error: Exception) {
            reduce { state.copy(errorMessage = error.message ?: "Login failed") }
        } finally {
            client?.close()
            reduce { state.copy(inProgress = false) }
        }
    }

    fun setError(message: String?) = intent {
        reduce { state.copy(errorMessage = message) }
    }
}
