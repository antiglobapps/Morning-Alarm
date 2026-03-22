package com.morningalarm.desktopadmin.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.morningalarm.desktopadmin.ui.login.LoginScreen
import com.morningalarm.desktopadmin.ui.login.LoginSideEffect
import com.morningalarm.desktopadmin.ui.login.LoginViewModel
import com.morningalarm.desktopadmin.ui.workspace.DesktopAdminWorkspace
import com.morningalarm.desktopadmin.ui.workspace.WorkspaceSideEffect
import org.koin.compose.koinInject
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
internal fun AppRouter(navigationController: NavigationController) {
    when (val screen = navigationController.currentScreen) {
        is Screen.Login -> {
            val viewModel = koinInject<LoginViewModel>()
            val state by viewModel.collectAsState()

            LaunchedEffect(screen.initialError) {
                viewModel.setError(screen.initialError)
            }

            viewModel.collectSideEffect { sideEffect ->
                when (sideEffect) {
                    is LoginSideEffect.LoginSuccess -> {
                        navigationController.replaceAll(Screen.Workspace(sideEffect.session))
                    }
                }
            }

            LoginScreen(
                viewModel = viewModel,
                state = state,
            )
        }

        is Screen.Workspace -> {
            DesktopAdminWorkspace(
                session = screen.session,
                onLogout = { message ->
                    navigationController.replaceAll(Screen.Login(initialError = message))
                },
            )
        }
    }
}
