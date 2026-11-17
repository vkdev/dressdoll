package ru.vkdev.dressdoll.doll.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.compose.koinInject
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import ru.vkdev.dressdoll.doll.api.DollRoute
import ru.vkdev.dressdoll.navigation.api.LocalNavigator
import ru.vkdev.dressdoll.settings.theme.api.ThemeSettingsRoute

@OptIn(KoinExperimentalAPI::class)
val dollModule = module {
    single {
        DollStateHolder(
            outfit = get(),
            repository = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        )
    }
    navigation<DollRoute> {
        val navigator = LocalNavigator.current
        DollScreen(
            holder = koinInject(),
            catalog = koinInject(),
            onOpenSettings = { navigator.navigate(ThemeSettingsRoute) },
        )
    }
}
