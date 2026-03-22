package com.morningalarm.desktopadmin.navigation

import com.morningalarm.desktopadmin.testAdminSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class NavigationControllerTest {

    private val testSession = testAdminSession()

    @Test
    fun `initial screen is set correctly`() {
        val controller = NavigationController(Screen.Login())
        assertIs<Screen.Login>(controller.currentScreen)
        assertFalse(controller.canGoBack)
    }

    @Test
    fun `navigateTo pushes screen onto stack`() {
        val controller = NavigationController(Screen.Login())

        controller.navigateTo(Screen.Workspace(testSession))

        assertIs<Screen.Workspace>(controller.currentScreen)
        assertTrue(controller.canGoBack)
    }

    @Test
    fun `goBack returns to previous screen`() {
        val controller = NavigationController(Screen.Login())
        controller.navigateTo(Screen.Workspace(testSession))

        val result = controller.goBack()

        assertTrue(result)
        assertIs<Screen.Login>(controller.currentScreen)
        assertFalse(controller.canGoBack)
    }

    @Test
    fun `goBack at root returns false`() {
        val controller = NavigationController(Screen.Login())

        val result = controller.goBack()

        assertFalse(result)
        assertIs<Screen.Login>(controller.currentScreen)
    }

    @Test
    fun `replaceAll clears back stack`() {
        val controller = NavigationController(Screen.Login())
        controller.navigateTo(Screen.Workspace(testSession))

        controller.replaceAll(Screen.Login())

        assertIs<Screen.Login>(controller.currentScreen)
        assertFalse(controller.canGoBack)
    }

    @Test
    fun `popBackTo navigates to matching screen`() {
        val controller = NavigationController(Screen.Login())
        controller.navigateTo(Screen.Workspace(testSession))

        controller.popBackTo { it is Screen.Login }

        assertIs<Screen.Login>(controller.currentScreen)
        assertFalse(controller.canGoBack)
    }

    @Test
    fun `popBackTo does nothing if no match`() {
        val controller = NavigationController(Screen.Login())

        controller.popBackTo { it is Screen.Workspace }

        assertIs<Screen.Login>(controller.currentScreen)
    }

    @Test
    fun `multiple navigations build correct stack`() {
        val controller = NavigationController(Screen.Login())

        controller.navigateTo(Screen.Workspace(testSession))
        assertIs<Screen.Workspace>(controller.currentScreen)
        assertTrue(controller.canGoBack)

        controller.goBack()
        assertIs<Screen.Login>(controller.currentScreen)
        assertFalse(controller.canGoBack)
    }

    @Test
    fun `replaceAll from workspace to login prevents back navigation`() {
        val controller = NavigationController(Screen.Login())
        controller.navigateTo(Screen.Workspace(testSession))

        controller.replaceAll(Screen.Login())

        assertFalse(controller.canGoBack)
        assertIs<Screen.Login>(controller.currentScreen)
    }

    @Test
    fun `login screen can carry initial error as navigation argument`() {
        val controller = NavigationController(Screen.Login())

        controller.replaceAll(Screen.Login(initialError = "Session expired"))

        val screen = assertIs<Screen.Login>(controller.currentScreen)
        assertFalse(controller.canGoBack)
        assertEquals("Session expired", screen.initialError)
    }
}
