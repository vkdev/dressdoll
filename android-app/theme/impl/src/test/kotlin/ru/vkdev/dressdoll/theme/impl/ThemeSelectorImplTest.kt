package ru.vkdev.dressdoll.theme.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import ru.vkdev.dressdoll.theme.api.SystemDefault
import ru.vkdev.dressdoll.theme.api.ThemeChoice
import ru.vkdev.dressdoll.theme.api.ThemePalette
import java.io.File
import java.io.IOException

class ThemeSelectorImplTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val file by lazy { File(tmp.root, "theme.preferences_pb") }

    // Свой экземпляр DataStore закрывается после block: следующий вызов читает файл, а не кэш.
    private suspend fun TestScope.withSelector(
        palettes: List<ThemePalette> = DAY_NIGHT,
        block: suspend (selector: ThemeSelectorImpl, dataStore: DataStore<Preferences>) -> Unit,
    ) {
        val job = Job(backgroundScope.coroutineContext[Job])
        val scope = CoroutineScope(backgroundScope.coroutineContext + job)
        val dataStore = createThemeDataStore(scope) { file }
        block(ThemeSelectorImpl(dataStore, palettes, scope), dataStore)
        job.cancelAndJoin()
    }

    private suspend fun DataStore<Preferences>.awaitSaved(choice: ThemeChoice) {
        data.first { it[ThemeSelectorImpl.KEY] == ThemeSelectorImpl.encode(choice) }
    }

    @Test
    fun `choice is null until loaded, then defaults to light palette`() = runTest {
        withSelector { selector, _ ->
            assertNull(selector.choice.value)
            assertEquals(ThemeChoice.Palette("day"), selector.choice.first { it != null })
        }
    }

    @Test
    fun `without light palette defaults to system`() = runTest {
        withSelector(palettes = listOf(testPalette("plain"))) { selector, _ ->
            assertEquals(ThemeChoice.System, selector.choice.first { it != null })
        }
    }

    @Test
    fun `selected choice is applied at once and read back from disk`() = runTest {
        withSelector { selector, dataStore ->
            selector.choice.first { it != null }
            selector.select(ThemeChoice.System)
            assertEquals(ThemeChoice.System, selector.choice.first { it == ThemeChoice.System })
            dataStore.awaitSaved(ThemeChoice.System)
        }
        withSelector { reloaded, _ ->
            assertEquals(ThemeChoice.System, reloaded.choice.first { it != null })
        }
    }

    @Test
    fun `last of quick selections wins`() = runTest {
        withSelector { selector, dataStore ->
            selector.select(ThemeChoice.Palette("night"))
            selector.select(ThemeChoice.System)
            selector.select(ThemeChoice.Palette("denim"))
            dataStore.awaitSaved(ThemeChoice.Palette("denim"))
            assertEquals(ThemeChoice.Palette("denim"), selector.choice.first { it != null })
        }
        withSelector { reloaded, _ ->
            assertEquals(ThemeChoice.Palette("denim"), reloaded.choice.first { it != null })
        }
    }

    @Test
    fun `corrupted file falls back to default and can be overwritten`() = runTest {
        file.writeBytes(byteArrayOf(0x7f, 0x01, 0x02, 0x03))
        withSelector { selector, dataStore ->
            assertEquals(ThemeChoice.Palette("day"), selector.choice.first { it != null })
            selector.select(ThemeChoice.System)
            dataStore.awaitSaved(ThemeChoice.System)
        }
        withSelector { reloaded, _ ->
            assertEquals(ThemeChoice.System, reloaded.choice.first { it != null })
        }
    }

    @Test
    fun `failed write keeps the choice for the session and does not crash`() = runTest {
        val selector = ThemeSelectorImpl(FailingWritesDataStore(), emptyList(), backgroundScope)
        selector.choice.first { it != null }

        selector.select(ThemeChoice.Palette("powder"))
        advanceUntilIdle()

        assertEquals(ThemeChoice.Palette("powder"), selector.choice.value)
        selector.select(ThemeChoice.System)
        advanceUntilIdle()
        assertEquals(ThemeChoice.System, selector.choice.value)
    }

    @Test
    fun `read error falls back to default and later choices still apply`() = runTest {
        val selector = ThemeSelectorImpl(FailingReadsDataStore(), DAY_NIGHT, backgroundScope)

        assertEquals(ThemeChoice.Palette("day"), selector.choice.first { it != null })
        selector.select(ThemeChoice.System)
        advanceUntilIdle()
        assertEquals(ThemeChoice.System, selector.choice.value)
    }

    @Test
    fun `palettes are sorted by order`() = runTest {
        val palettes = listOf(2, 0, 1).mapIndexed { index, order -> testPalette("p$index", order) }
        withSelector(palettes = palettes) { selector, _ ->
            assertEquals(listOf("p1", "p2", "p0"), selector.palettes.map { it.id })
        }
    }

    @Test
    fun `unknown stored value falls back to default`() {
        assertNull(ThemeSelectorImpl.decode("garbage"))
        assertNull(ThemeSelectorImpl.decode(null))
    }

    @Test
    fun `encode and decode are symmetric`() {
        listOf(ThemeChoice.System, ThemeChoice.Palette("powder")).forEach { choice ->
            assertEquals(choice, ThemeSelectorImpl.decode(ThemeSelectorImpl.encode(choice)))
        }
    }

    private companion object {
        val DAY_NIGHT = listOf(
            testPalette("day", systemDefault = SystemDefault.LIGHT),
            testPalette("night", order = 1, systemDefault = SystemDefault.DARK),
        )
    }

    private class FailingWritesDataStore : DataStore<Preferences> {
        override val data: Flow<Preferences> = flowOf(emptyPreferences())
        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
            throw IOException("disk full")
    }

    private class FailingReadsDataStore : DataStore<Preferences> {
        override val data: Flow<Preferences> = kotlinx.coroutines.flow.flow { throw IOException("read failed") }
        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
            throw IOException("read failed")
    }
}
