package ru.vkdev.dressdoll.navigation.api

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavKey

interface Navigator {

    val canGoBack: Boolean

    fun navigate(route: NavKey)

    /** Возвращает false, если экран последний: он остаётся на месте. */
    fun back(): Boolean
}

val LocalNavigator = staticCompositionLocalOf<Navigator> { error("Navigator is not provided") }
