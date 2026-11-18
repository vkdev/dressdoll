package ru.vkdev.dressdoll.theme.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.vkdev.dressdoll.core.coroutines.applicationScope
import ru.vkdev.dressdoll.theme.api.AppTheme
import ru.vkdev.dressdoll.theme.api.ThemePalette
import ru.vkdev.dressdoll.theme.api.ThemeSelector

private val ThemeDataStore = named("theme")

val themeModule = module {
    single<DataStore<Preferences>>(ThemeDataStore) {
        createThemeDataStore(scope = applicationScope { io }) {
            androidContext().preferencesDataStoreFile("theme")
        }
    }
    single<ThemeSelector> {
        ThemeSelectorImpl(
            dataStore = get(ThemeDataStore),
            palettes = getAll<ThemePalette>(),
            scope = applicationScope { default },
        )
    }
    single<AppTheme> { AppThemeImpl(selector = get()) }
}
