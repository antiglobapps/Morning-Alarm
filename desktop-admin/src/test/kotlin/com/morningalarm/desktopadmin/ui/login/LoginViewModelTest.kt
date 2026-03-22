package com.morningalarm.desktopadmin.ui.login

import com.morningalarm.desktopadmin.config.AppPreferences
import com.morningalarm.desktopadmin.config.ConnectionMode
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.test.test
import kotlin.test.Test

class LoginViewModelTest {

    private fun createViewModel() = LoginViewModel(
        preferences = AppPreferences(),
        apiClientFactory = { throw IllegalStateException("No server in tests") },
    )

    @Test
    fun `initial state has dev connection mode and default credentials`() = runTest {
        createViewModel().test(this) {
            expectInitialState()
        }
    }

    @Test
    fun `updateConnectionMode changes connection mode`() = runTest {
        createViewModel().test(this) {
            expectInitialState()

            containerHost.updateConnectionMode(ConnectionMode.CUSTOM)
            expectState { copy(connectionMode = ConnectionMode.CUSTOM) }
        }
    }

    @Test
    fun `updateCustomBaseUrl changes custom URL`() = runTest {
        createViewModel().test(this) {
            expectInitialState()

            containerHost.updateCustomBaseUrl("https://prod.example.com")
            expectState { copy(customBaseUrl = "https://prod.example.com") }
        }
    }

    @Test
    fun `updateEmail changes email`() = runTest {
        createViewModel().test(this) {
            expectInitialState()

            containerHost.updateEmail("user@test.com")
            expectState { copy(email = "user@test.com") }
        }
    }

    @Test
    fun `updatePassword changes password`() = runTest {
        createViewModel().test(this) {
            expectInitialState()

            containerHost.updatePassword("secret123")
            expectState { copy(password = "secret123") }
        }
    }

    @Test
    fun `updateAdminSecret changes admin secret`() = runTest {
        createViewModel().test(this) {
            expectInitialState()

            containerHost.updateAdminSecret("my-admin-secret")
            expectState { copy(adminSecret = "my-admin-secret") }
        }
    }

    @Test
    fun `setError sets error message`() = runTest {
        createViewModel().test(this) {
            expectInitialState()

            containerHost.setError("Session expired")
            expectState { copy(errorMessage = "Session expired") }
        }
    }

    @Test
    fun `setError with null clears error`() = runTest {
        createViewModel().test(this) {
            expectInitialState()

            containerHost.setError("Some error")
            expectState { copy(errorMessage = "Some error") }

            containerHost.setError(null)
            expectState { copy(errorMessage = null) }
        }
    }

    @Test
    fun `login sets inProgress and shows error when API fails`() = runTest {
        createViewModel().test(this) {
            expectInitialState()

            containerHost.login()

            expectState { copy(errorMessage = null, inProgress = true) }
            expectState { copy(errorMessage = "No server in tests", inProgress = true) }
            expectState { copy(inProgress = false) }
        }
    }
}
