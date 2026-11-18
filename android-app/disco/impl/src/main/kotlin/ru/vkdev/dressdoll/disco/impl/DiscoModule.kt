package ru.vkdev.dressdoll.disco.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.vkdev.dressdoll.core.coroutines.applicationScope
import ru.vkdev.dressdoll.disco.api.Disco
import ru.vkdev.dressdoll.settings.api.SettingsSection

private val DiscoDataStore = named("disco")

val discoModule = module {
    single<DataStore<Preferences>>(DiscoDataStore) {
        createDiscoDataStore(scope = applicationScope { io }) {
            androidContext().preferencesDataStoreFile("disco")
        }
    }
    single<DiscoSettingsRepository> {
        DiscoSettingsRepositoryImpl(
            dataStore = get(DiscoDataStore),
            scope = applicationScope { default },
        )
    }
    single<Disco> { DiscoImpl(settings = get()) }
    single<SettingsSection>(named("disco")) { DiscoSettingsSection(settings = get()) }
}
