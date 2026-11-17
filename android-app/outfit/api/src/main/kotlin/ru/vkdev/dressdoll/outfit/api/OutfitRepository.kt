package ru.vkdev.dressdoll.outfit.api

import kotlinx.coroutines.flow.StateFlow

/**
 * Хранит надетое. Правила надевания и проверку по датасету применяет потребитель:
 * репозиторий возвращает ровно то, что сохранили.
 */
interface OutfitRepository {
    /** Надетые вещи в порядке надевания; `null`, пока образ не прочитан с диска. */
    val worn: StateFlow<List<WornItem>?>

    /** [worn] меняется сразу, на диск образ пишется в фоне. */
    fun save(worn: List<WornItem>)
}
