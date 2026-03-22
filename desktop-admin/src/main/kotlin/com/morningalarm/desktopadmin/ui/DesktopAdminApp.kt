package com.morningalarm.desktopadmin.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.morningalarm.desktopadmin.navigation.AppRouter
import com.morningalarm.desktopadmin.navigation.Screen
import com.morningalarm.desktopadmin.navigation.rememberNavigationController
import com.morningalarm.dto.auth.AuthSessionDto

internal data class AdminSession(
    val baseUrl: String,
    val adminSecret: String,
    val authSession: AuthSessionDto,
)

@Composable
fun DesktopAdminApp() {
    val navigationController = rememberNavigationController(initialScreen = Screen.Login())

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFFFB86B),
            secondary = Color(0xFF9BD1FF),
            background = Color(0xFF10151F),
            surface = Color(0xFF17202F),
        ),
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            AppRouter(navigationController)
        }
    }
}
