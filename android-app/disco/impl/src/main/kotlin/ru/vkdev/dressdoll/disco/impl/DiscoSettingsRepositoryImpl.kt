package ru.vkdev.dressdoll.disco.impl

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.vkdev.dressdoll.disco.api.BeamStyle
import ru.vkdev.dressdoll.disco.api.DiscoSettingsRepository
import java.io.File
import java.io.IOException

// Выбор применяется сразу, в памяти, а на диск пишется в фоне: сбой чтения или записи
// не роняет приложение, выбор действует до перезапуска.
internal class DiscoSettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    scope: CoroutineScope,
) : DiscoSettingsRepository {

    @Volatile
    private var selected: BeamStyle? = null

    private val current = MutableStateFlow(DEFAULT_STYLE)

    // Один потребитель сохраняет порядок нажатий, CONFLATED оставляет только последний выбор.
    private val pendingWrites = Channel<BeamStyle>(Channel.CONFLATED)

    override val beamStyle: StateFlow<BeamStyle> = current.asStateFlow()

    init {
        scope.launch {
            dataStore.data
                .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
                .map { prefs -> decode(prefs[KEY]) ?: DEFAULT_STYLE }
                // update атомарен: выбор, сделанный до чтения с диска, не затирается сохранённым.
                .collect { saved -> current.update { selected ?: saved } }
        }
        scope.launch {
            for (style in pendingWrites) {
                try {
                    dataStore.edit { prefs -> prefs[KEY] = style.name }
                } catch (_: IOException) {
                }
            }
        }
    }

    override fun setBeamStyle(style: BeamStyle) {
        selected = style
        current.value = style
        pendingWrites.trySend(style)
    }

    internal companion object {
        val KEY = stringPreferencesKey("beam_style")
        val DEFAULT_STYLE = BeamStyle.PAPER

        // Неизвестное имя — стиль из будущей версии или удалённый: берётся стиль по умолчанию.
        fun decode(value: String?): BeamStyle? = BeamStyle.entries.firstOrNull { it.name == value }
    }
}

internal fun createDiscoDataStore(scope: CoroutineScope, file: () -> File): DataStore<Preferences> =
    PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        scope = scope,
        produceFile = file,
    )
