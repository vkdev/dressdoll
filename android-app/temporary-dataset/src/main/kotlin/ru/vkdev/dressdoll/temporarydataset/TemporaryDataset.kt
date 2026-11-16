package ru.vkdev.dressdoll.temporarydataset

import android.content.res.AssetManager
import android.graphics.Bitmap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import ru.vkdev.dressdoll.temporarydataset.bitmap.AssetBitmapDecoder
import ru.vkdev.dressdoll.temporarydataset.bitmap.BitmapRepository
import ru.vkdev.dressdoll.temporarydataset.bitmap.TDImageKey
import java.io.FileNotFoundException

internal const val DATASET_ROOT = "cloth-src"

/**
 * Датасет одежды в ассетах: картинки в `cloth-src/<category>/<item>/`.
 * Свойства категорий сгенерированы при сборке из `categories.json` модуля.
 */
class TemporaryDataset internal constructor(
    private val assets: AssetManager,
    private val ioDispatcher: CoroutineDispatcher,
    private val bitmaps: BitmapRepository,
) {

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

    // Читается только внутри flow на ioDispatcher, поэтому список ассетов не читается на главном потоке.
    private val categories: List<TDClothingCategory> by lazy { matchFolders(categoriesConfig.categories) }

    /** Порядок из конфига: по разделам каталога, внутри раздела — как в `categories.json`. */
    fun clothingCategories(): Flow<List<TDClothingCategory>> = flow {
        emit(categories)
    }.flowOn(ioDispatcher)

    fun catalogSections(): Flow<List<TDSection>> = flowOf(categoriesConfig.sections)

    fun clothingItems(category: CategoryName): Flow<List<TDClothingItem>> = flow {
        emit(
            list("$DATASET_ROOT/${category.value}").map { folder ->
                TDClothingItem(id = TDItemId(category, folder), name = "${category.value} $folder")
            },
        )
    }.flowOn(ioDispatcher)

    suspend fun catalogImage(item: TDItemId): Bitmap? = bitmaps.load(TDImageKey(item, TDImageKind.CATALOG))

    suspend fun dollImages(item: TDItemId): TDDollImages = coroutineScope {
        val front = async { require(TDImageKey(item, TDImageKind.FRONT)) }
        val back = async { bitmaps.load(TDImageKey(item, TDImageKind.BACK)) }
        TDDollImages(front = front.await(), back = back.await())
    }

    /** Вызывать из `Application.onTrimMemory`. */
    fun trimMemory(level: Int) = bitmaps.trimMemory(level)

    // Сборка уже сверила конфиг с папками; здесь страховка на случай нестандартного ignoreAssetsPattern.
    // NFC не нужен: имена не в NFC сборка не пропускает.
    private fun matchFolders(configured: List<TDClothingCategory>): List<TDClothingCategory> {
        val folders = list(DATASET_ROOT).toSet()
        val names = configured.mapTo(mutableSetOf()) { it.name.value }
        check(folders == names) {
            "categories.json does not match $DATASET_ROOT: " +
                "folders without entry ${folders - names}, entries without folder ${names - folders}"
        }
        return configured
    }

    private suspend fun require(key: TDImageKey): Bitmap =
        bitmaps.load(key) ?: throw FileNotFoundException("No image ${key.assetPath}")

    // Некоторые реализации AssetManager (например, Robolectric) отдают вложенные пути вида
    // "01/front.png", поэтому берётся только первый сегмент.
    private fun list(path: String): List<String> =
        assets.list(path).orEmpty()
            .map { it.substringBefore('/') }
            .distinct()
            .sorted()

    private companion object {
        // Каждому кешу 1/16 кучи: хватает на одетую куклу или экран каталога.
        fun defaultCacheBytes(): Int = (Runtime.getRuntime().maxMemory() / 16).toInt()
    }
}
