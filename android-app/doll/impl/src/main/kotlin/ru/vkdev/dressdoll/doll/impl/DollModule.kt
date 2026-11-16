package ru.vkdev.dressdoll.doll.impl

import org.koin.compose.koinInject
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import ru.vkdev.dressdoll.doll.api.DollRoute
import ru.vkdev.dressdoll.navigation.api.LocalNavigator
import ru.vkdev.dressdoll.settings.theme.api.ThemeSettingsRoute

@OptIn(KoinExperimentalAPI::class)
val dollModule = module {
    navigation<DollRoute> {
        val navigator = LocalNavigator.current
        DollScreen(
            catalog = koinInject(),
            onOpenSettings = { navigator.navigate(ThemeSettingsRoute) },
        )
    }
}
