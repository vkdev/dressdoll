package ru.vkdev.dressdoll.navigation.api

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey

interface AppNavigation {

    /**
     * [startRoute] используется только при первом создании бэкстека: после восстановления
     * из saved state новое значение игнорируется.
     */
    @Composable
    fun Host(startRoute: NavKey)
}
