package ru.vkdev.dressdoll.core.datastore

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
import java.io.IOException

// Значение меняется сразу, в памяти, а на диск пишется в фоне: сбой чтения или записи
// не роняет приложение, значение действует до перезапуска.
class PersistedValue<S, T>(
    private val dataStore: DataStore<S>,
    scope: CoroutineScope,
    initial: T,
    /** Что считать прочитанным, если чтение упало с `IOException`. */
    onReadError: S,
    read: (S) -> T,
    private val write: (S, T) -> S,
) {

    // Коробка отличает «ничего не выбрано» от выбранного null.
    private class Chosen<T>(val value: T)

    @Volatile
    private var chosen: Chosen<T>? = null

    private val current = MutableStateFlow(initial)

    // Один потребитель сохраняет порядок записей, CONFLATED оставляет только последнее значение.
    private val pendingWrites = Channel<T>(Channel.CONFLATED)

    val value: StateFlow<T> = current.asStateFlow()

    init {
        scope.launch {
            dataStore.data
                // После ошибки поток завершается, но новые значения всё равно идут через set().
                .catch { error -> if (error is IOException) emit(onReadError) else throw error }
                .map(read)
                // update атомарен: если set() успел поменять значение, повтор прочитает
                // новый chosen и не затрёт его прочитанным с диска.
                .collect { stored ->
                    current.update {
                        val choice = chosen
                        if (choice != null) choice.value else stored
                    }
                }
        }
        scope.launch {
            for (value in pendingWrites) {
                try {
                    dataStore.updateData { stored -> write(stored, value) }
                } catch (_: IOException) {
                }
            }
        }
    }

    fun set(value: T) {
        chosen = Chosen(value)
        current.value = value
        pendingWrites.trySend(value)
    }
}
