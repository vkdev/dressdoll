package ru.vkdev.dressdoll.temporarydataset.bitmap

import android.content.ComponentCallbacks2
import android.graphics.Bitmap
import android.util.LruCache
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import ru.vkdev.dressdoll.temporarydataset.TDImageKind
import java.util.concurrent.ConcurrentHashMap

/**
 * Loads bitmaps through [decoder] and keeps them in two LRU caches limited by bytes:
 * catalog images and doll images are cached separately, so scrolling the catalog
 * does not evict the items currently worn on the doll.
 *
 * Concurrent requests for the same image share one decode.
 * Evicted bitmaps are not recycled: the UI may still be drawing them.
 */
internal class BitmapRepository(
    private val decoder: BitmapDecoder,
    ioDispatcher: CoroutineDispatcher,
    catalogCacheBytes: Int,
    dollCacheBytes: Int,
) {
    val catalogCache: LruCache<TDImageKey, Bitmap> = bitmapLruCache(catalogCacheBytes)
    val dollCache: LruCache<TDImageKey, Bitmap> = bitmapLruCache(dollCacheBytes)

    // Images known to be absent (e.g. items without back.png); LruCache cannot store null.
    private val missing: MutableSet<TDImageKey> = ConcurrentHashMap.newKeySet()
    private val inFlight = HashMap<TDImageKey, Deferred<Bitmap?>>()
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    /** Returns the cached or freshly decoded bitmap, or null if the image does not exist. */
    suspend fun load(key: TDImageKey): Bitmap? {
        val cache = cacheFor(key.kind)
        val deferred = synchronized(inFlight) {
            cache.get(key)?.let { return it }
            if (key in missing) return null
            inFlight.getOrPut(key) { decodeAsync(key, cache) }
        }
        return deferred.await()
    }

    /** Releases memory on [ComponentCallbacks2.onTrimMemory]. */
    @Suppress("DEPRECATION") // TRIM_MEMORY_RUNNING_* are still delivered on older APIs.
    fun trimMemory(level: Int) {
        // RUNNING_CRITICAL (15) < UI_HIDDEN (20) < BACKGROUND (40): UI hidden or worse drops the catalog.
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL) {
            catalogCache.evictAll()
        }
        if (level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND ||
            level == ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL
        ) {
            dollCache.evictAll()
        }
    }

    private fun decodeAsync(key: TDImageKey, cache: LruCache<TDImageKey, Bitmap>): Deferred<Bitmap?> =
        scope.async {
            try {
                decoder.decode(key).also { bitmap ->
                    if (bitmap == null) missing += key else cache.put(key, bitmap)
                }
            } finally {
                // Removed after the result is cached, so later callers hit the cache.
                synchronized(inFlight) { inFlight.remove(key) }
            }
        }

    private fun cacheFor(kind: TDImageKind): LruCache<TDImageKey, Bitmap> = when (kind) {
        TDImageKind.CATALOG -> catalogCache
        TDImageKind.FRONT, TDImageKind.BACK -> dollCache
    }
}

/** LRU cache sized by bitmap bytes: `get` moves an entry to the top, the least recently used is evicted. */
internal fun bitmapLruCache(maxBytes: Int): LruCache<TDImageKey, Bitmap> =
    object : LruCache<TDImageKey, Bitmap>(maxBytes) {
        override fun sizeOf(key: TDImageKey, value: Bitmap): Int = value.allocationByteCount
    }
