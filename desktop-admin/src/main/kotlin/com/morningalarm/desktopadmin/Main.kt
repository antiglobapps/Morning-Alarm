package com.morningalarm.desktopadmin

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.morningalarm.desktopadmin.ui.DesktopAdminApp

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Morning Alarm Desktop Admin",
    ) {
        DesktopAdminApp()
    }
}
