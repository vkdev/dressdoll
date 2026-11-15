package ru.vkdev.dressdoll.doll.impl

import org.koin.dsl.module
import ru.vkdev.dressdoll.doll.api.DollScreen

val dollModule = module {
    single<DollScreen> { DollScreenImpl(repository = get()) }
}
