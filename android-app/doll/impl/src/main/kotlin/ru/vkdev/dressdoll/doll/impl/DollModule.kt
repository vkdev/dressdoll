package ru.vkdev.dressdoll.doll.impl

import org.koin.compose.koinInject
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import ru.vkdev.dressdoll.core.coroutines.applicationScope
import ru.vkdev.dressdoll.doll.api.DollRoute
import ru.vkdev.dressdoll.navigation.api.LocalNavigator
import ru.vkdev.dressdoll.settings.api.SettingsRoute

val dollModule = module {
    single {
        DollStateHolder(
            outfit = get(),
            repository = get(),
            scope = applicationScope { default },
        )
    }
    navigation<DollRoute> {
        val navigator = LocalNavigator.current
        DollScreen(
            holder = koinInject(),
            catalog = koinInject(),
            disco = koinInject(),
            onOpenSettings = { navigator.navigate(SettingsRoute) },
        )
    }
}
