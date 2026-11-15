package ru.vkdev.dressdoll.navigation.api

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavKey

interface Navigator {

    /** В бэкстеке есть куда вернуться: экранов больше одного. */
    val canGoBack: Boolean

    /** Кладёт [route] на вершину бэкстека. */
    fun navigate(route: NavKey)

    /** Убирает верхний экран. Возвращает false, если это последний экран и он остался на месте. */
    fun back(): Boolean

    /** Заменяет весь бэкстек одним [route]. */
    fun replaceAll(route: NavKey)
}

val LocalNavigator = staticCompositionLocalOf<Navigator> { error("Navigator is not provided") }
