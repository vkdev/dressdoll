package ru.vkdev.dressdoll.clothing.impl

import android.content.Context
import org.koin.dsl.module
import ru.vkdev.dressdoll.clothing.api.ClothingRepository
import ru.vkdev.dressdoll.core.coroutines.AppDispatchers
import ru.vkdev.dressdoll.core.coroutines.ApplicationScope
import ru.vkdev.dressdoll.temporarydataset.TemporaryDataset

val clothingModule = module {
    single {
        TemporaryDataset(
            assets = get<Context>().assets,
            scope = get(ApplicationScope),
            ioDispatcher = get<AppDispatchers>().io,
            config = ClothingConfig.dataset(),
        )
    }
    single<ClothingRepository> { ClothingRepositoryImpl(dataset = get()) }
    // Кеши освобождаются по сигналам системы, даже если репозиторий ещё никто не запросил.
    single(createdAtStart = true) {
        val dataset = get<TemporaryDataset>()
        TrimMemoryCallbacks(dataset::trimMemory).also { get<Context>().registerComponentCallbacks(it) }
    }
}
