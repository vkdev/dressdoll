package ru.vkdev.dressdoll.core.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.plus
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module

/** Скоуп без диспетчера на всё время работы приложения: диспетчер добавляет потребитель. */
val ApplicationScope: Qualifier = named("ApplicationScope")

val coroutinesModule = module {
    // SupervisorJob: сбой одного потребителя не отменяет остальных.
    single(ApplicationScope) { CoroutineScope(SupervisorJob()) }
    single { AppDispatchers(io = Dispatchers.IO, default = Dispatchers.Default) }
}

fun Scope.applicationScope(dispatcher: AppDispatchers.() -> CoroutineDispatcher): CoroutineScope =
    get<CoroutineScope>(ApplicationScope) + get<AppDispatchers>().dispatcher()
