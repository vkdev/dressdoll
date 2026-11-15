package ru.vkdev.dressdoll.theme.impl

import org.koin.dsl.module
import ru.vkdev.dressdoll.theme.api.AppTheme

val themeModule = module {
    single<AppTheme> { AppThemeImpl() }
}
