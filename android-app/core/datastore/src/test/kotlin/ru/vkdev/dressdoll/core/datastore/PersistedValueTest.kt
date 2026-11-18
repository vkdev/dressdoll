package ru.vkdev.dressdoll.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class PersistedValueTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val file by lazy { File(tmp.root, "value.txt") }

    // Свой экземпляр DataStore закрывается после block: следующий вызов читает файл, а не кэш.
    private suspend fun TestScope.withValue(
        block: suspend (value: PersistedValue<String, String?>, dataStore: DataStore<String>) -> Unit,
    ) {
        val job = Job(backgroundScope.coroutineContext[Job])
        val scope = CoroutineScope(backgroundScope.coroutineContext + job)
        val dataStore = DataStoreFactory.create(serializer = TextSerializer, scope = scope) { file }
        block(persisted(dataStore, scope), dataStore)
        job.cancelAndJoin()
    }

    private fun persisted(dataStore: DataStore<String>, scope: CoroutineScope) = PersistedValue<String, String?>(
        dataStore = dataStore,
        scope = scope,
        initial = null,
        onReadError = READ_ERROR,
        read = { it },
        write = { _, value -> value.orEmpty() },
    )

    @Test
    fun `value is initial until read, then stored value`() = runTest {
        file.writeText("красный")
        withValue { value, _ ->
            assertNull(value.value.value)
            assertEquals("красный", value.value.first { it != null })
        }
    }

    @Test
    fun `set applies at once and is read back after restart`() = runTest {
        withValue { value, dataStore ->
            value.set("синий")
            assertEquals("синий", value.value.value)
            dataStore.data.first { it == "синий" }
        }
        withValue { reloaded, _ ->
            assertEquals("синий", reloaded.value.first { it != null })
        }
    }

    @Test
    fun `last of quick sets wins`() = runTest {
        withValue { value, dataStore ->
            value.set("красный")
            value.set("зелёный")
            value.set("синий")
            dataStore.data.first { it == "синий" }
            assertEquals("синий", value.value.value)
        }
        withValue { reloaded, _ ->
            assertEquals("синий", reloaded.value.first { it != null })
        }
    }

    @Test
    fun `set before first read is not overwritten by stored value`() = runTest {
        val stored = MutableSharedFlow<String>()
        val value = persisted(DelayedReadsDataStore(stored), backgroundScope)
        advanceUntilIdle()

        value.set("синий")
        stored.emit("красный")
        advanceUntilIdle()

        assertEquals("синий", value.value.value)
    }

    @Test
    fun `chosen null is not overwritten by stored value`() = runTest {
        val stored = MutableSharedFlow<String>()
        val value = persisted(DelayedReadsDataStore(stored), backgroundScope)
        advanceUntilIdle()

        value.set(null)
        stored.emit("красный")
        advanceUntilIdle()

        assertNull(value.value.value)
    }

    @Test
    fun `read error falls back to onReadError and later sets still apply`() = runTest {
        val value = persisted(FailingReadsDataStore(), backgroundScope)

        assertEquals(READ_ERROR, value.value.first { it != null })
        value.set("синий")
        advanceUntilIdle()
        assertEquals("синий", value.value.value)
    }

    @Test
    fun `failed write keeps the value for the session and does not crash`() = runTest {
        val value = persisted(FailingWritesDataStore(), backgroundScope)
        value.value.first { it != null }

        value.set("красный")
        advanceUntilIdle()
        assertEquals("красный", value.value.value)

        value.set("синий")
        advanceUntilIdle()
        assertEquals("синий", value.value.value)
    }

    private object TextSerializer : Serializer<String> {
        override val defaultValue = ""
        override suspend fun readFrom(input: InputStream): String = input.readBytes().decodeToString()
        override suspend fun writeTo(t: String, output: OutputStream) = output.write(t.encodeToByteArray())
    }

    private class FailingWritesDataStore : DataStore<String> {
        override val data: Flow<String> = flowOf("")
        override suspend fun updateData(transform: suspend (t: String) -> String): String =
            throw IOException("disk full")
    }

    private class FailingReadsDataStore : DataStore<String> {
        override val data: Flow<String> = flow { throw IOException("read failed") }
        override suspend fun updateData(transform: suspend (t: String) -> String): String =
            throw IOException("read failed")
    }

    // Отдаёт содержимое файла, только когда тест его выпустит, — чтобы set() успел раньше.
    private class DelayedReadsDataStore(override val data: Flow<String>) : DataStore<String> {
        override suspend fun updateData(transform: suspend (t: String) -> String): String = transform("")
    }

    private companion object {
        const val READ_ERROR = "сбой чтения"
    }
}
