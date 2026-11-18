package ru.vkdev.dressdoll.core.coroutines

import kotlinx.coroutines.CoroutineDispatcher

class AppDispatchers(
    val io: CoroutineDispatcher,
    val default: CoroutineDispatcher,
)
