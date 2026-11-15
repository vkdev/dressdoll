package ru.vkdev.dressdoll.temporarydataset

import android.content.res.AssetManager
import android.graphics.Bitmap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import ru.vkdev.dressdoll.temporarydataset.bitmap.AssetBitmapDecoder
import ru.vkdev.dressdoll.temporarydataset.bitmap.BitmapRepository
import ru.vkdev.dressdoll.temporarydataset.bitmap.TDImageKey
import java.io.FileNotFoundException

internal const val DATASET_ROOT = "cloth-src"

/** Read access to the clothing dataset stored in assets under `cloth-src/<category>/<item>/`. */
class TemporaryDataset internal constructor(
    private val assets: AssetManager,
    private val ioDispatcher: CoroutineDispatcher,
    private val bitmaps: BitmapRepository,
) {

    /**
     * @param catalogCacheBytes memory budget for decoded catalog images.
     * @param dollCacheBytes memory budget for decoded doll images (front and back).
     */
    constructor(
        assets: AssetManager,
        ioDispatcher: CoroutineDispatcher,
        catalogCacheBytes: Int = defaultCacheBytes(),
        dollCacheBytes: Int = defaultCacheBytes(),
    ) : this(
        assets = assets,
        ioDispatcher = ioDispatcher,
        bitmaps = BitmapRepository(AssetBitmapDecoder(assets), ioDispatcher, catalogCacheBytes, dollCacheBytes),
    )

    fun clothingCategories(): Flow<List<TDClothingCategory>> = flow {
        emit(list(DATASET_ROOT).map { TDClothingCategory(CategoryName(it)) })
    }.flowOn(ioDispatcher)

    fun clothingItems(category: CategoryName): Flow<List<TDClothingItem>> = flow {
        emit(
            list("$DATASET_ROOT/${category.value}").map { folder ->
                TDClothingItem(id = TDItemId(category, folder), name = "${category.value} $folder")
            },
        )
    }.flowOn(ioDispatcher)

    /** Image of the whole item for the catalog, or null if the item has none. */
    suspend fun catalogImage(item: TDItemId): Bitmap? = bitmaps.load(TDImageKey(item, TDImageKind.CATALOG))

    /** Front and optional back plane of the item for the doll. */
    suspend fun dollImages(item: TDItemId): TDDollImages = coroutineScope {
        val front = async { require(TDImageKey(item, TDImageKind.FRONT)) }
        val back = async { bitmaps.load(TDImageKey(item, TDImageKind.BACK)) }
        TDDollImages(front = front.await(), back = back.await())
    }

    /** Call from `Application.onTrimMemory` to release cached bitmaps. */
    fun trimMemory(level: Int) = bitmaps.trimMemory(level)

    private suspend fun require(key: TDImageKey): Bitmap =
        bitmaps.load(key) ?: throw FileNotFoundException("No image ${key.assetPath}")

    // Direct children of [path]. Some AssetManager implementations (e.g. Robolectric)
    // return nested entries like "01/front.png", so keep only the first segment.
    private fun list(path: String): List<String> =
        assets.list(path).orEmpty()
            .map { it.substringBefore('/') }
            .distinct()
            .sorted()

    private companion object {
        // Each cache gets 1/16 of the heap: enough for a dressed doll or a screen of catalog.
        fun defaultCacheBytes(): Int = (Runtime.getRuntime().maxMemory() / 16).toInt()
    }
}
