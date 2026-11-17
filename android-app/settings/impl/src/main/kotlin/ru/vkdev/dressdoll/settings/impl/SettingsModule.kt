package ru.vkdev.dressdoll.settings.impl

import androidx.compose.runtime.remember
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import ru.vkdev.dressdoll.navigation.api.LocalNavigator
import ru.vkdev.dressdoll.settings.api.SettingsRoute
import ru.vkdev.dressdoll.settings.api.SettingsSection

val settingsModule = module {
    single<SettingsSection>(named("theme")) { ThemeSettingsSection(selector = get()) }
    navigation<SettingsRoute> {
        val navigator = LocalNavigator.current
        // Запоминаем при входе: при уходе назад бэкстек укорачивается раньше, чем кончается
        // анимация, и без remember стрелка пропадала бы на уходящем экране.
        val canGoBack = remember { navigator.canGoBack }
        val sections = remember { getAll<SettingsSection>().sortedBy { it.order } }
        SettingsScreen(
            sections = sections,
            onBack = if (canGoBack) ({ navigator.back() }) else null,
        )
    }
}
