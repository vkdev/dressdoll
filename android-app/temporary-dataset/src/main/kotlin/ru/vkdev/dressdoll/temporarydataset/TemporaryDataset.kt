package ru.vkdev.dressdoll.temporarydataset

import android.content.res.AssetManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/** Read access to the clothing dataset stored in assets under `cloth-src/<category>/<item>/`. */
class TemporaryDataset(
    private val assets: AssetManager,
    private val ioDispatcher: CoroutineDispatcher,
) {

    fun clothingCategories(): Flow<List<TDClothingCategory>> = flow {
        emit(list(ROOT).map { TDClothingCategory(CategoryName(it)) })
    }.flowOn(ioDispatcher)

    fun clothingItems(category: CategoryName): Flow<List<TDClothingItem>> = flow {
        emit(
            list("$ROOT/${category.value}").map { folder ->
                TDClothingItem(name = "${category.value} $folder", parentCategory = category)
            },
        )
    }.flowOn(ioDispatcher)

    // Direct children of [path]. Some AssetManager implementations (e.g. Robolectric)
    // return nested entries like "01/front.png", so keep only the first segment.
    private fun list(path: String): List<String> =
        assets.list(path).orEmpty()
            .map { it.substringBefore('/') }
            .distinct()
            .sorted()

    private companion object {
        const val ROOT = "cloth-src"
    }
}
