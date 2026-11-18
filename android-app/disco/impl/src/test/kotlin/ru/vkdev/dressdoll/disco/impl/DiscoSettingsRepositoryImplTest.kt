package ru.vkdev.dressdoll.disco.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class DiscoSettingsRepositoryImplTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val file by lazy { File(tmp.root, "disco.preferences_pb") }

    // Свой экземпляр DataStore закрывается после block: следующий вызов читает файл, а не кэш.
    private suspend fun TestScope.withRepository(
        block: suspend (repository: DiscoSettingsRepositoryImpl, dataStore: DataStore<Preferences>) -> Unit,
    ) {
        val job = Job(backgroundScope.coroutineContext[Job])
        val scope = CoroutineScope(backgroundScope.coroutineContext + job)
        val dataStore = createDiscoDataStore(scope) { file }
        block(DiscoSettingsRepositoryImpl(dataStore, scope), dataStore)
        job.cancelAndJoin()
    }

    @Test
    fun `по умолчанию бумажные конусы`() = runTest {
        withRepository { repository, _ ->
            advanceUntilIdle()
            assertEquals(BeamStyle.PAPER, repository.beamStyle.value)
        }
    }

    @Test
    fun `выбор применяется сразу и читается с диска после перезапуска`() = runTest {
        withRepository { repository, dataStore ->
            repository.setBeamStyle(BeamStyle.SOFT)
            assertEquals(BeamStyle.SOFT, repository.beamStyle.value)
            dataStore.data.first { it[DiscoSettingsRepositoryImpl.KEY] == BeamStyle.SOFT.name }
        }
        withRepository { reloaded, _ ->
            assertEquals(BeamStyle.SOFT, reloaded.beamStyle.first { it == BeamStyle.SOFT })
        }
    }

    @Test
    fun `неизвестное значение на диске — стиль по умолчанию`() = runTest {
        withRepository { _, dataStore ->
            dataStore.edit { it[DiscoSettingsRepositoryImpl.KEY] = "NEON" }
        }
        withRepository { repository, _ ->
            advanceUntilIdle()
            assertEquals(BeamStyle.PAPER, repository.beamStyle.value)
        }
    }
}
