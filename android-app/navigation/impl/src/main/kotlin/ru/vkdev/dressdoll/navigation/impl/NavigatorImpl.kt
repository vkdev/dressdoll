package ru.vkdev.dressdoll.navigation.impl

import androidx.navigation3.runtime.NavKey
import ru.vkdev.dressdoll.navigation.api.Navigator

internal class NavigatorImpl(
    private val backStack: MutableList<NavKey>,
) : Navigator {

    override val canGoBack: Boolean
        get() = backStack.size > 1

    override fun navigate(route: NavKey) {
        backStack.add(route)
    }

    override fun back(): Boolean {
        if (!canGoBack) return false
        backStack.removeAt(backStack.lastIndex)
        return true
    }

    override fun replaceAll(route: NavKey) {
        backStack.add(route)
        backStack.subList(0, backStack.lastIndex).clear()
    }
}
