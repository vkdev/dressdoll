package ru.vkdev.dressdoll.navigation.api

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey

/** Корневой хост навигации: показывает экраны по бэкстеку. */
interface AppNavigation {

    /**
     * [startRoute] кладётся в бэкстек только при его первом создании. Дальше бэкстек
     * восстанавливается из saved state (поворот экрана, смерть процесса), поэтому
     * новое значение [startRoute] при перекомпозиции или восстановлении игнорируется.
     * Сменить экран у работающего хоста можно через [Navigator.replaceAll].
     */
    @Composable
    fun Host(startRoute: NavKey)
}
