package ru.vkdev.dressdoll.outfit.impl

import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import ru.vkdev.dressdoll.outfit.api.WornItem
import ru.vkdev.dressdoll.repository.api.CategoryId
import ru.vkdev.dressdoll.repository.api.ClothingItemId
import java.io.File
import java.io.IOException

class OutfitRepositoryImplTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val file by lazy { File(tmp.root, "outfit.json") }

    private val boots = item("сапоги", "01")
    private val skirt = item("юбка", "02")
    private val blouse = item("блузка", "01")

    // Свой экземпляр DataStore закрывается после block: следующий вызов читает файл, а не кэш.
    private suspend fun TestScope.withRepository(
        block: suspend (repository: OutfitRepositoryImpl, dataStore: DataStore<OutfitFile>) -> Unit,
    ) {
        val job = Job(backgroundScope.coroutineContext[Job])
        val scope = CoroutineScope(backgroundScope.coroutineContext + job)
        val dataStore = createOutfitDataStore(scope) { file }
        block(OutfitRepositoryImpl(dataStore, scope), dataStore)
        job.cancelAndJoin()
    }

    private suspend fun DataStore<OutfitFile>.awaitSaved(worn: List<WornItem>) {
        val expected = worn.map { StoredItem(it.id.category.value, it.id.key) }
        data.first { it.worn == expected }
    }

    @Test
    fun `worn is null until loaded, then empty when there is no file`() = runTest {
        withRepository { repository, _ ->
            assertNull(repository.worn.value)
            assertEquals(emptyList<WornItem>(), repository.worn.first { it != null })
        }
    }

    @Test
    fun `saved outfit is applied at once and read back from disk`() = runTest {
        val outfit = listOf(boots, skirt)
        withRepository { repository, dataStore ->
            repository.worn.first { it != null }
            repository.save(outfit)
            assertEquals(outfit, repository.worn.value)
            dataStore.awaitSaved(outfit)
        }
        withRepository { reloaded, _ ->
            assertEquals(outfit, reloaded.worn.first { it != null })
        }
    }

    @Test
    fun `order and duplicates are kept as saved`() = runTest {
        val outfit = listOf(skirt, boots, skirt, blouse)
        withRepository { repository, dataStore ->
            repository.save(outfit)
            dataStore.awaitSaved(outfit)
        }
        withRepository { reloaded, _ ->
            assertEquals(outfit, reloaded.worn.first { it != null })
        }
    }

    @Test
    fun `last of quick saves wins`() = runTest {
        withRepository { repository, dataStore ->
            repository.save(listOf(boots))
            repository.save(listOf(skirt))
            repository.save(listOf(blouse, boots))
            dataStore.awaitSaved(listOf(blouse, boots))
            assertEquals(listOf(blouse, boots), repository.worn.first { it != null })
        }
        withRepository { reloaded, _ ->
            assertEquals(listOf(blouse, boots), reloaded.worn.first { it != null })
        }
    }

    @Test
    fun `corrupted file falls back to empty outfit and can be overwritten`() = runTest {
        file.writeText("{ not json")
        withRepository { repository, dataStore ->
            assertEquals(emptyList<WornItem>(), repository.worn.first { it != null })
            repository.save(listOf(boots))
            dataStore.awaitSaved(listOf(boots))
        }
        withRepository { reloaded, _ ->
            assertEquals(listOf(boots), reloaded.worn.first { it != null })
        }
    }

    @Test
    fun `failed write keeps the outfit for the session and does not crash`() = runTest {
        val repository = OutfitRepositoryImpl(FailingWritesDataStore(), backgroundScope)
        repository.worn.first { it != null }

        repository.save(listOf(boots))
        advanceUntilIdle()
        assertEquals(listOf(boots), repository.worn.value)

        repository.save(listOf(skirt))
        advanceUntilIdle()
        assertEquals(listOf(skirt), repository.worn.value)
    }

    @Test
    fun `read error falls back to empty outfit and later saves still apply`() = runTest {
        val repository = OutfitRepositoryImpl(FailingReadsDataStore(), backgroundScope)

        assertEquals(emptyList<WornItem>(), repository.worn.first { it != null })
        repository.save(listOf(boots))
        advanceUntilIdle()
        assertEquals(listOf(boots), repository.worn.value)
    }

    @Test
    fun `save before first read is not overwritten by stored outfit`() = runTest {
        val stored = MutableSharedFlow<OutfitFile>()
        val repository = OutfitRepositoryImpl(DelayedReadsDataStore(stored), backgroundScope)
        advanceUntilIdle()

        repository.save(listOf(boots))
        stored.emit(OutfitFile(worn = listOf(StoredItem("юбка", "02"))))
        advanceUntilIdle()

        assertEquals(listOf(boots), repository.worn.value)
    }

    @Test
    fun `file with unknown field is read`() = runTest {
        file.writeText(
            """{"version":1,"worn":[{"category":"сапоги","key":"01","tucked":true}],"savedLooks":[]}""",
        )
        withRepository { repository, _ ->
            assertEquals(listOf(boots), repository.worn.first { it != null })
        }
    }

    @Test
    fun `written file contains format version`() = runTest {
        withRepository { repository, dataStore ->
            repository.save(listOf(boots))
            dataStore.awaitSaved(listOf(boots))
        }
        val written = Json.parseToJsonElement(file.readText()).jsonObject
        assertEquals(1, written["version"]?.jsonPrimitive?.int)
    }

    private fun item(category: String, key: String) = WornItem(ClothingItemId(CategoryId(category), key))

    private class FailingWritesDataStore : DataStore<OutfitFile> {
        override val data: Flow<OutfitFile> = flowOf(OutfitFile())
        override suspend fun updateData(transform: suspend (t: OutfitFile) -> OutfitFile): OutfitFile =
            throw IOException("disk full")
    }

    private class FailingReadsDataStore : DataStore<OutfitFile> {
        override val data: Flow<OutfitFile> = flow { throw IOException("read failed") }
        override suspend fun updateData(transform: suspend (t: OutfitFile) -> OutfitFile): OutfitFile =
            throw IOException("read failed")
    }

    // Отдаёт содержимое файла, только когда тест его выпустит, — чтобы save() успел раньше.
    private class DelayedReadsDataStore(override val data: Flow<OutfitFile>) : DataStore<OutfitFile> {
        override suspend fun updateData(transform: suspend (t: OutfitFile) -> OutfitFile): OutfitFile =
            transform(OutfitFile())
    }
}
