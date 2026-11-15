package ru.vkdev.dressdoll.repository.api

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow

interface DollRepository {

    fun clothingCategories(): Flow<List<ClothingCategory>>

    fun clothingItems(category: CategoryId): Flow<List<ClothingItem>>

    /** Image of the whole item for the catalog, or null if the item has none. */
    suspend fun catalogImage(item: ClothingItemId): Bitmap?

    /** Front and optional back plane of the item for the doll. */
    suspend fun dollImages(item: ClothingItemId): DollImages

    /** Call from `ComponentCallbacks2.onTrimMemory`: releases caches depending on [level]. */
    fun onTrimMemory(level: Int)

    /** Call from `ComponentCallbacks.onLowMemory`: releases all caches. */
    fun onLowMemory()
}
