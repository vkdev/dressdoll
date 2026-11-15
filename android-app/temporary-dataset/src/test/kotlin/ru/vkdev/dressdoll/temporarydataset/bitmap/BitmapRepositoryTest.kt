package ru.vkdev.dressdoll.temporarydataset.bitmap

import android.content.ComponentCallbacks2
import android.graphics.Bitmap
import kotlinx.coroutines.async
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.vkdev.dressdoll.temporarydataset.CategoryName
import ru.vkdev.dressdoll.temporarydataset.TDImageKind
import ru.vkdev.dressdoll.temporarydataset.TDItemId
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class BitmapRepositoryTest {

    private val dispatcher = StandardTestDispatcher()
    private val decoded = mutableListOf<TDImageKey>()
    private var absent = emptySet<TDImageKey>()

    private val decoder = BitmapDecoder { key ->
        decoded += key
        if (key in absent) null else Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
    }

    @Test
    fun `cached image is decoded once`() = runTest(dispatcher) {
        val repo = repository()

        val first = repo.load(A_FRONT)
        val second = repo.load(A_FRONT)

        assertSame(first, second)
        assertEquals(listOf(A_FRONT), decoded)
    }

    @Test
    fun `concurrent requests share one decode`() = runTest(dispatcher) {
        val repo = repository()

        val first = async { repo.load(A_FRONT) }
        val second = async { repo.load(A_FRONT) }

        assertSame(first.await(), second.await())
        assertEquals(listOf(A_FRONT), decoded)
    }

    @Test
    fun `missing image is null and remembered`() = runTest(dispatcher) {
        absent = setOf(A_BACK)
        val repo = repository()

        assertNull(repo.load(A_BACK))
        assertNull(repo.load(A_BACK))
        assertEquals(listOf(A_BACK), decoded)
    }

    @Test
    fun `least recently used image is evicted`() = runTest(dispatcher) {
        val repo = repository(dollCacheBytes = 2 * BITMAP_BYTES)

        repo.load(A_FRONT)
        repo.load(B_FRONT)
        repo.load(A_FRONT) // A moves to the top, B becomes least recently used
        repo.load(C_FRONT) // evicts B

        assertNotNull(repo.dollCache.get(A_FRONT))
        assertNull(repo.dollCache.get(B_FRONT))
        assertNotNull(repo.dollCache.get(C_FRONT))
    }

    @Test
    fun `catalog images do not evict doll images`() = runTest(dispatcher) {
        val repo = repository(catalogCacheBytes = BITMAP_BYTES, dollCacheBytes = BITMAP_BYTES)

        repo.load(A_FRONT)
        repo.load(A_CATALOG)
        repo.load(B_CATALOG)

        assertNotNull(repo.dollCache.get(A_FRONT))
        assertNull(repo.catalogCache.get(A_CATALOG))
        assertNotNull(repo.catalogCache.get(B_CATALOG))
    }

    @Test
    fun `failed decode is retried on next request`() = runTest(dispatcher) {
        var fail = true
        val repo = BitmapRepository(
            decoder = { if (fail) throw IOException("broken") else Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) },
            ioDispatcher = dispatcher,
            catalogCacheBytes = CACHE_BYTES,
            dollCacheBytes = CACHE_BYTES,
        )

        runCatching { repo.load(A_FRONT) }
        fail = false

        assertNotNull(repo.load(A_FRONT))
    }

    @Test
    fun `trimMemory UI_HIDDEN drops catalog only, BACKGROUND drops both`() = runTest(dispatcher) {
        val repo = repository()
        repo.load(A_FRONT)
        repo.load(A_CATALOG)

        repo.trimMemory(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN)
        assertEquals(0, repo.catalogCache.size())
        assertNotNull(repo.dollCache.get(A_FRONT))

        repo.trimMemory(ComponentCallbacks2.TRIM_MEMORY_BACKGROUND)
        assertEquals(0, repo.dollCache.size())
    }

    private fun repository(
        catalogCacheBytes: Int = CACHE_BYTES,
        dollCacheBytes: Int = CACHE_BYTES,
    ) = BitmapRepository(decoder, dispatcher, catalogCacheBytes, dollCacheBytes)

    private companion object {
        const val BITMAP_BYTES = 10 * 10 * 4
        const val CACHE_BYTES = 100 * BITMAP_BYTES

        fun key(folder: String, kind: TDImageKind) = TDImageKey(TDItemId(CategoryName("x"), folder), kind)

        val A_FRONT = key("a", TDImageKind.FRONT)
        val A_BACK = key("a", TDImageKind.BACK)
        val A_CATALOG = key("a", TDImageKind.CATALOG)
        val B_FRONT = key("b", TDImageKind.FRONT)
        val B_CATALOG = key("b", TDImageKind.CATALOG)
        val C_FRONT = key("c", TDImageKind.FRONT)
    }
}
