package ru.vkdev.dressdoll.navigation.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import org.koin.compose.navigation3.koinEntryProvider
import ru.vkdev.dressdoll.navigation.api.AppNavigation
import ru.vkdev.dressdoll.navigation.api.LocalNavigator

internal class AppNavigationImpl : AppNavigation {

    @Composable
    override fun Host(startRoute: NavKey) {
        val backStack = rememberNavBackStack(startRoute)
        val navigator = remember(backStack) { NavigatorImpl(backStack) }
        CompositionLocalProvider(LocalNavigator provides navigator) {
            NavDisplay(
                backStack = backStack,
                onBack = { navigator.back() },
                entryProvider = koinEntryProvider<NavKey>(),
            )
        }
    }
}
