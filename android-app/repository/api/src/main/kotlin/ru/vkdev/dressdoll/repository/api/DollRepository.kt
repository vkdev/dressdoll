package ru.vkdev.dressdoll.repository.api

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow

interface DollRepository {

    /** Категории идут по разделам в порядке [catalogSections]. */
    fun clothingCategories(): Flow<List<ClothingCategory>>

    fun catalogSections(): Flow<List<CatalogSection>>

    fun clothingItems(category: CategoryId): Flow<List<ClothingItem>>

    suspend fun catalogImage(item: ClothingItemId): Bitmap?

    suspend fun dollImages(item: ClothingItemId): DollImages

    /** Вызывать из `ComponentCallbacks2.onTrimMemory`. */
    fun onTrimMemory(level: Int)

    /** Вызывать из `ComponentCallbacks.onLowMemory`: сбрасывает все кеши. */
    fun onLowMemory()
}
