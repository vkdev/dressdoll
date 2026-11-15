package ru.vkdev.dressdoll.doll.impl

import org.koin.compose.koinInject
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import ru.vkdev.dressdoll.doll.api.DollRoute

@OptIn(KoinExperimentalAPI::class)
val dollModule = module {
    navigation<DollRoute> { DollScreen(repository = koinInject()) }
}
