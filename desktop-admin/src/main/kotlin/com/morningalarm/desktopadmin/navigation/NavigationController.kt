package com.morningalarm.desktopadmin.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Lightweight navigation controller with back stack support.
 *
 * Manages a stack of [Screen] destinations. The top of the stack is the currently visible screen.
 * Supports forward navigation, back navigation, and replacing the entire stack.
 */
@Stable
internal class NavigationController(initialScreen: Screen) {

    private var backStack: List<Screen> by mutableStateOf(listOf(initialScreen))

    val currentScreen: Screen
        get() = backStack.last()

    val canGoBack: Boolean
        get() = backStack.size > 1

    /** Push a new screen onto the stack. */
    fun navigateTo(screen: Screen) {
        backStack = backStack + screen
    }

    /** Pop the current screen and return to the previous one. Returns false if already at root. */
    fun goBack(): Boolean {
        if (!canGoBack) return false
        backStack = backStack.dropLast(1)
        return true
    }

    /**
     * Replace the entire back stack with a single screen.
     * Useful for login → workspace transition where back navigation should not return to login.
     */
    fun replaceAll(screen: Screen) {
        backStack = listOf(screen)
    }

    /**
     * Pop back to the first screen matching the predicate, removing everything above it.
     * If no match, the stack is unchanged.
     */
    fun popBackTo(predicate: (Screen) -> Boolean) {
        val index = backStack.indexOfLast(predicate)
        if (index >= 0) {
            backStack = backStack.take(index + 1)
        }
    }
}

@Composable
internal fun rememberNavigationController(initialScreen: Screen): NavigationController {
    return remember { NavigationController(initialScreen) }
}
