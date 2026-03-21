package com.morningalarm.desktopadmin.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.morningalarm.desktopadmin.config.AppPreferences
import com.morningalarm.desktopadmin.data.AdminApiClient
import com.morningalarm.dto.auth.AuthSessionDto
import kotlinx.coroutines.launch

internal data class AdminSession(
    val baseUrl: String,
    val adminSecret: String,
    val authSession: AuthSessionDto,
)

@Composable
fun DesktopAdminApp() {
    val preferences = remember { AppPreferences() }
    val scope = rememberCoroutineScope()
    var session by remember { mutableStateOf<AdminSession?>(null) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var loginInProgress by remember { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFFFB86B),
            secondary = Color(0xFF9BD1FF),
            background = Color(0xFF10151F),
            surface = Color(0xFF17202F),
        ),
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            if (session == null) {
                LoginScreen(
                    preferences = preferences,
                    errorMessage = loginError,
                    inProgress = loginInProgress,
                    onLogin = { baseUrl, email, password, adminSecret ->
                        scope.launch {
                            loginError = null
                            loginInProgress = true
                            val client = AdminApiClient(baseUrl)
                            runCatching {
                                val authSession = client.adminLogin(email, password, adminSecret)
                                session = AdminSession(
                                    baseUrl = baseUrl,
                                    adminSecret = adminSecret,
                                    authSession = authSession,
                                )
                            }.onFailure { error ->
                                loginError = error.message ?: "Login failed"
                            }
                            client.close()
                            loginInProgress = false
                        }
                    },
                )
            } else {
                DesktopAdminWorkspace(
                    session = session!!,
                    onLogout = { message ->
                        loginError = message
                        session = null
                    },
                )
            }
        }
    }
}
