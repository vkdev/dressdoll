package ru.vkdev.dressdoll.disco.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.vkdev.dressdoll.disco.api.DiscoSettingsRepository

private val DiscoDataStore = named("disco")

val discoModule = module {
    single<DataStore<Preferences>>(DiscoDataStore) {
        createDiscoDataStore(scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)) {
            androidContext().preferencesDataStoreFile("disco")
        }
    }
    single<DiscoSettingsRepository> {
        DiscoSettingsRepositoryImpl(
            dataStore = get(DiscoDataStore),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        )
    }
}
