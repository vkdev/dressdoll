package ru.vkdev.dressdoll.theme.impl

import androidx.datastore.core.DataStore
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
import ru.vkdev.dressdoll.theme.api.ThemeChoice
import ru.vkdev.dressdoll.theme.api.ThemeIds
import ru.vkdev.dressdoll.theme.api.ThemePalette
import ru.vkdev.dressdoll.theme.api.ThemeSelector
import java.io.IOException

// Выбор применяется сразу, в памяти, а на диск пишется в фоне: сбой чтения или записи
// не роняет приложение и не замораживает тему, выбор действует до перезапуска.
internal class ThemeSelectorImpl(
    private val dataStore: DataStore<Preferences>,
    palettes: List<ThemePalette>,
    private val scope: CoroutineScope,
) : ThemeSelector {

    override val palettes: List<ThemePalette> = palettes.sortedBy { it.order }

    @Volatile
    private var selected: ThemeChoice? = null

    private val current = MutableStateFlow<ThemeChoice?>(null)

    // Один потребитель сохраняет порядок нажатий, CONFLATED оставляет только последний выбор.
    private val pendingWrites = Channel<ThemeChoice>(Channel.CONFLATED)

    override val choice: StateFlow<ThemeChoice?> = current.asStateFlow()

    init {
        scope.launch {
            dataStore.data
                // После ошибки поток завершается, но новые выборы всё равно идут через select().
                .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
                .map { prefs -> decode(prefs[KEY]) ?: DEFAULT_CHOICE }
                // update атомарен: если select() успел поменять значение, повтор прочитает
                // новый selected и не затрёт выбор сохранённым.
                .collect { saved -> current.update { selected ?: saved } }
        }
        scope.launch {
            for (choice in pendingWrites) {
                try {
                    dataStore.edit { prefs -> prefs[KEY] = encode(choice) }
                } catch (_: IOException) {
                }
            }
        }
    }

    override fun select(choice: ThemeChoice) {
        selected = choice
        current.value = choice
        pendingWrites.trySend(choice)
    }

    internal companion object {
        val KEY = stringPreferencesKey("theme_choice")
        val DEFAULT_CHOICE: ThemeChoice = ThemeChoice.Palette(ThemeIds.CINNAMON)

        private const val SYSTEM = "system"
        private const val PALETTE_PREFIX = "palette:"

        fun encode(choice: ThemeChoice): String = when (choice) {
            ThemeChoice.System -> SYSTEM
            is ThemeChoice.Palette -> PALETTE_PREFIX + choice.id
        }

        fun decode(value: String?): ThemeChoice? = when {
            value == SYSTEM -> ThemeChoice.System
            value != null && value.startsWith(PALETTE_PREFIX) ->
                ThemeChoice.Palette(value.removePrefix(PALETTE_PREFIX))
            else -> null
        }
    }
}
