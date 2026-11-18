package ru.vkdev.dressdoll.outfit.impl

import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.vkdev.dressdoll.core.coroutines.applicationScope
import ru.vkdev.dressdoll.outfit.api.OutfitRepository

private val OutfitDataStore = named("outfit")

val outfitModule = module {
    single<DataStore<OutfitFile>>(OutfitDataStore) {
        createOutfitDataStore(scope = applicationScope { io }) {
            androidContext().dataStoreFile("outfit.json")
        }
    }
    single<OutfitRepository> {
        OutfitRepositoryImpl(
            dataStore = get(OutfitDataStore),
            scope = applicationScope { default },
        )
    }
}
