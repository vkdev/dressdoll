package ru.vkdev.dressdoll.settings.theme.impl

import org.koin.compose.koinInject
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import ru.vkdev.dressdoll.navigation.api.LocalNavigator
import ru.vkdev.dressdoll.settings.theme.api.ThemeSettingsRoute

val themeSettingsModule = module {
    navigation<ThemeSettingsRoute> {
        val navigator = LocalNavigator.current
        ThemeSettingsScreen(
            selector = koinInject(),
            onBack = if (navigator.canGoBack) ({ navigator.back() }) else null,
        )
    }
}
