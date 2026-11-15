package ru.vkdev.dressdoll.repository.impl

import android.content.ComponentCallbacks2
import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.vkdev.dressdoll.repository.api.CategoryId
import ru.vkdev.dressdoll.repository.api.ClothingCategory
import ru.vkdev.dressdoll.repository.api.ClothingItem
import ru.vkdev.dressdoll.repository.api.ClothingItemId
import ru.vkdev.dressdoll.repository.api.DollImages
import ru.vkdev.dressdoll.repository.api.DollRepository
import ru.vkdev.dressdoll.temporarydataset.TemporaryDataset

internal class DollRepositoryImpl(
    private val dataset: TemporaryDataset,
) : DollRepository {

    override fun clothingCategories(): Flow<List<ClothingCategory>> =
        dataset.clothingCategories().map { categories -> categories.map { it.toClothingCategory() } }

    override fun clothingItems(category: CategoryId): Flow<List<ClothingItem>> =
        dataset.clothingItems(category.toCategoryName()).map { items -> items.map { it.toClothingItem() } }

    override suspend fun catalogImage(item: ClothingItemId): Bitmap? =
        dataset.catalogImage(item.toTDItemId())

    override suspend fun dollImages(item: ClothingItemId): DollImages =
        dataset.dollImages(item.toTDItemId()).toDollImages()

    override fun onTrimMemory(level: Int) = dataset.trimMemory(level)

    @Suppress("DEPRECATION") // The strongest level: drops every cache.
    override fun onLowMemory() = dataset.trimMemory(ComponentCallbacks2.TRIM_MEMORY_COMPLETE)
}
