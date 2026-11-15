package ru.vkdev.dressdoll.repository.impl

import android.content.Context
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module
import ru.vkdev.dressdoll.repository.api.DollRepository
import ru.vkdev.dressdoll.temporarydataset.TemporaryDataset

val repositoryModule = module {
    single { TemporaryDataset(assets = get<Context>().assets, ioDispatcher = Dispatchers.IO) }
    single<DollRepository> { DollRepositoryImpl(dataset = get()) }
}
