package ru.vkdev.dressdoll.catalog.impl

import org.koin.dsl.module
import ru.vkdev.dressdoll.catalog.api.Catalog

val catalogModule = module {
    single<Catalog> { CatalogImpl(repository = get()) }
}
