package ru.vkdev.dressdoll.outfit.impl

import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.vkdev.dressdoll.outfit.api.OutfitRepository
import ru.vkdev.dressdoll.outfit.api.WornItem
import ru.vkdev.dressdoll.repository.api.CategoryId
import ru.vkdev.dressdoll.repository.api.ClothingItemId
import java.io.IOException

// Образ меняется сразу, в памяти, а на диск пишется в фоне: сбой чтения или записи
// не роняет приложение, образ действует до перезапуска.
internal class OutfitRepositoryImpl(
    private val dataStore: DataStore<OutfitFile>,
    scope: CoroutineScope,
) : OutfitRepository {

    @Volatile
    private var saved: List<WornItem>? = null

    private val current = MutableStateFlow<List<WornItem>?>(null)

    // Один потребитель сохраняет порядок записей, CONFLATED оставляет только последний образ.
    private val pendingWrites = Channel<List<WornItem>>(Channel.CONFLATED)

    override val worn: StateFlow<List<WornItem>?> = current.asStateFlow()

    init {
        scope.launch {
            dataStore.data
                // После ошибки поток завершается, но новые образы всё равно идут через save().
                .catch { error -> if (error is IOException) emit(OutfitFile()) else throw error }
                .map { file -> file.worn.map { it.toWornItem() } }
                // update атомарен: если save() успел поменять значение, повтор прочитает
                // новый saved и не затрёт образ прочитанным с диска.
                .collect { stored -> current.update { saved ?: stored } }
        }
        scope.launch {
            for (items in pendingWrites) {
                try {
                    dataStore.updateData { file -> file.copy(worn = items.map { it.toStored() }) }
                } catch (_: IOException) {
                }
            }
        }
    }

    override fun save(worn: List<WornItem>) {
        saved = worn
        current.value = worn
        pendingWrites.trySend(worn)
    }

    private fun StoredItem.toWornItem() = WornItem(ClothingItemId(CategoryId(category), key))

    private fun WornItem.toStored() = StoredItem(category = id.category.value, key = id.key)
}
