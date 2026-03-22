package com.morningalarm.desktopadmin

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.morningalarm.desktopadmin.di.desktopAdminModule
import com.morningalarm.desktopadmin.ui.DesktopAdminApp
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        modules(desktopAdminModule)
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Morning Alarm Desktop Admin",
        ) {
            DesktopAdminApp()
        }
    }
}
