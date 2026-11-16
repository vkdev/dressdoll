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

/** Датасет одежды в ассетах: картинки в `cloth-src/<category>/<item>/`, свойства категорий в `categories.json`. */
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

    // Читается только внутри flow на ioDispatcher, поэтому ассет не открывается на главном потоке.
    private val config: TDCategoriesConfig by lazy {
        CategoriesConfigParser.parse(assets.open(CATEGORIES_CONFIG).bufferedReader().use { it.readText() })
    }

    private val categories: List<TDClothingCategory> by lazy { matchFolders(config.categories) }

    /** Порядок из конфига: по разделам каталога, внутри раздела — как в `categories.json`. */
    fun clothingCategories(): Flow<List<TDClothingCategory>> = flow {
        emit(categories)
    }.flowOn(ioDispatcher)

    fun catalogSections(): Flow<List<TDSection>> = flow {
        emit(config.sections)
    }.flowOn(ioDispatcher)

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

    // Имена сравниваются в NFC, но для путей к ассетам остаётся написание самой папки.
    private fun matchFolders(configured: List<TDClothingCategory>): List<TDClothingCategory> {
        val folders = list(DATASET_ROOT).associateBy { it.toNfc() }
        val names = configured.map { it.name.value }
        val notConfigured = folders.keys - names.toSet()
        val noFolder = names - folders.keys
        check(notConfigured.isEmpty() && noFolder.isEmpty()) {
            "$CATEGORIES_CONFIG does not match $DATASET_ROOT: " +
                "folders without entry $notConfigured, entries without folder $noFolder"
        }
        return configured.map { it.copy(name = CategoryName(folders.getValue(it.name.value))) }
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
