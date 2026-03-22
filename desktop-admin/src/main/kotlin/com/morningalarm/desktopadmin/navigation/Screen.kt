package com.morningalarm.desktopadmin.navigation

import com.morningalarm.desktopadmin.ui.AdminSession

/**
 * Type-safe navigation destinations for the desktop admin app.
 * Each screen is a data object or data class carrying its required arguments.
 */
internal sealed interface Screen {

    data class Login(val initialError: String? = null) : Screen

    data class Workspace(val session: AdminSession) : Screen
}
