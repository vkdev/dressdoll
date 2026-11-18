package ru.vkdev.dressdoll.catalog.impl

import org.koin.dsl.module
import ru.vkdev.dressdoll.catalog.api.Catalog
import ru.vkdev.dressdoll.core.coroutines.applicationScope

val catalogModule = module {
    single {
        CatalogStateHolder(
            repository = get(),
            scope = applicationScope { default },
        )
    }
    single<Catalog> { CatalogImpl(holder = get()) }
}
