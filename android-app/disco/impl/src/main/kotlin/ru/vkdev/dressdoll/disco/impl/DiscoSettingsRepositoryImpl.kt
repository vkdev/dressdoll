package ru.vkdev.dressdoll.disco.impl

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import ru.vkdev.dressdoll.core.datastore.PersistedValue
import java.io.File

internal class DiscoSettingsRepositoryImpl(
    dataStore: DataStore<Preferences>,
    scope: CoroutineScope,
) : DiscoSettingsRepository {

    private val persisted = PersistedValue(
        dataStore = dataStore,
        scope = scope,
        initial = DEFAULT_STYLE,
        onReadError = emptyPreferences(),
        read = { prefs -> decode(prefs[KEY]) ?: DEFAULT_STYLE },
        write = { prefs, style -> prefs.toMutablePreferences().apply { this[KEY] = style.name } },
    )

    override val beamStyle: StateFlow<BeamStyle> = persisted.value

    override fun setBeamStyle(style: BeamStyle) = persisted.set(style)

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
