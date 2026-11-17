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
internal const val DOLL_IMAGE = "doll_1.png"

/**
 * Датасет одежды в ассетах: картинки в `cloth-src/<category>/<item>/`, кукла — `doll_1.png`.
 * Свойства категорий сгенерированы при сборке из `categories.json` модуля,
 * холст куклы и позиции вещей — из `doll_1.png` и `scale.json` вещей.
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
        emit(itemsOf(category))
    }.flowOn(ioDispatcher)

    /** Все вещи датасета в порядке [clothingCategories], внутри категории — по имени папки. */
    fun allClothingItems(): Flow<List<TDClothingItem>> = flow {
        emit(categories.flatMap { itemsOf(it.name) })
    }.flowOn(ioDispatcher)

    suspend fun catalogImage(item: TDItemId): Bitmap? = bitmaps.load(TDImageKey.Item(item, TDImageKind.CATALOG))

    /** @throws IllegalStateException если у вещи нет позиции: сборка генерирует её для каждой папки вещи. */
    suspend fun dollImages(item: TDItemId): TDDollImages = coroutineScope {
        val topLeft = checkNotNull(itemPlacements[item]) { "No placement for ${item.category.value}/${item.folder}" }
        val front = async { require(TDImageKey.Item(item, TDImageKind.FRONT)) }
        val back = async { bitmaps.load(TDImageKey.Item(item, TDImageKind.BACK)) }
        TDDollImages(front = front.await(), back = back.await(), topLeft = topLeft)
    }

    suspend fun figure(): TDDollFigure = TDDollFigure(
        image = require(TDImageKey.Figure),
        canvasWidth = dollConfig.canvasWidth,
        canvasHeight = dollConfig.canvasHeight,
        bounds = dollConfig.figure,
    )

    /** Вызывать из `Application.onTrimMemory`. */
    fun trimMemory(level: Int) = bitmaps.trimMemory(level)

    // Сборка уже сверила конфиг с папками; здесь страховка на случай нестандартного ignoreAssetsPattern.
    private fun matchFolders(configured: List<TDClothingCategory>): List<TDClothingCategory> {
        val folders = list(DATASET_ROOT).toSet()
        val names = configured.mapTo(mutableSetOf()) { it.name.value }
        check(folders == names) {
            "categories.json does not match $DATASET_ROOT: " +
                "folders without entry ${folders - names}, entries without folder ${names - folders}"
        }
        return configured
    }

    private fun itemsOf(category: CategoryName): List<TDClothingItem> =
        list("$DATASET_ROOT/${category.value}").map { folder ->
            TDClothingItem(id = TDItemId(category, folder), name = "${category.value} $folder")
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
