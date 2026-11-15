package ru.vkdev.dressdoll.navigation.impl

import org.koin.dsl.module
import ru.vkdev.dressdoll.navigation.api.AppNavigation

val navigationModule = module {
    single<AppNavigation> { AppNavigationImpl() }
}
