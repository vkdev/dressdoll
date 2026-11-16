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
 * Картинки каталога и куклы лежат в разных LRU-кешах, чтобы прокрутка каталога
 * не вытесняла вещи, надетые на куклу.
 *
 * Параллельные запросы одной картинки делят одно декодирование.
 * Вытесненные битмапы не recycle(): UI может ещё их рисовать.
 */
internal class BitmapRepository(
    private val decoder: BitmapDecoder,
    ioDispatcher: CoroutineDispatcher,
    catalogCacheBytes: Int,
    dollCacheBytes: Int,
) {
    val catalogCache: LruCache<TDImageKey, Bitmap> = bitmapLruCache(catalogCacheBytes)
    val dollCache: LruCache<TDImageKey, Bitmap> = bitmapLruCache(dollCacheBytes)

    // Картинки, которых точно нет (например, back.png у части вещей): LruCache не хранит null.
    private val missing: MutableSet<TDImageKey> = ConcurrentHashMap.newKeySet()
    private val inFlight = HashMap<TDImageKey, Deferred<Bitmap?>>()
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    /** `null`, если картинки нет. */
    suspend fun load(key: TDImageKey): Bitmap? {
        val cache = cacheFor(key.kind)
        val deferred = synchronized(inFlight) {
            cache.get(key)?.let { return it }
            if (key in missing) return null
            inFlight.getOrPut(key) { decodeAsync(key, cache) }
        }
        return deferred.await()
    }

    @Suppress("DEPRECATION") // TRIM_MEMORY_RUNNING_* всё ещё приходят на старых API.
    fun trimMemory(level: Int) {
        // RUNNING_CRITICAL (15) < UI_HIDDEN (20) < BACKGROUND (40): от скрытого UI и хуже сбрасывается каталог.
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
                // Удаляется после записи в кеш, поэтому следующие запросы попадают в кеш.
                synchronized(inFlight) { inFlight.remove(key) }
            }
        }

    private fun cacheFor(kind: TDImageKind): LruCache<TDImageKey, Bitmap> = when (kind) {
        TDImageKind.CATALOG -> catalogCache
        TDImageKind.FRONT, TDImageKind.BACK -> dollCache
    }
}

internal fun bitmapLruCache(maxBytes: Int): LruCache<TDImageKey, Bitmap> =
    object : LruCache<TDImageKey, Bitmap>(maxBytes) {
        override fun sizeOf(key: TDImageKey, value: Bitmap): Int = value.allocationByteCount
    }
