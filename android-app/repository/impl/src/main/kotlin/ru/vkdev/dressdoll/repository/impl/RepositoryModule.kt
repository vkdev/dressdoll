package ru.vkdev.dressdoll.repository.impl

import org.koin.dsl.module
import ru.vkdev.dressdoll.repository.api.DollRepository

val repositoryModule = module {
    single<DollRepository> { DollRepositoryImpl() }
}
