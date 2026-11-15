package ru.vkdev.dressdoll.settings.theme.impl

import androidx.compose.runtime.remember
import org.koin.compose.koinInject
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import ru.vkdev.dressdoll.navigation.api.LocalNavigator
import ru.vkdev.dressdoll.settings.theme.api.ThemeSettingsRoute

val themeSettingsModule = module {
    navigation<ThemeSettingsRoute> {
        val navigator = LocalNavigator.current
        // Запоминаем при входе: при уходе назад бэкстек укорачивается раньше, чем кончается
        // анимация, и без remember стрелка пропадала бы на уходящем экране.
        val canGoBack = remember { navigator.canGoBack }
        ThemeSettingsScreen(
            selector = koinInject(),
            onBack = if (canGoBack) ({ navigator.back() }) else null,
        )
    }
}
