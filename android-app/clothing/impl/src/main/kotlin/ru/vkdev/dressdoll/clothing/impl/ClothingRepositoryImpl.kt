package ru.vkdev.dressdoll.clothing.impl

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.vkdev.dressdoll.clothing.api.CategoryId
import ru.vkdev.dressdoll.clothing.api.ClothingCategory
import ru.vkdev.dressdoll.clothing.api.ClothingItem
import ru.vkdev.dressdoll.clothing.api.ClothingItemId
import ru.vkdev.dressdoll.clothing.api.ClothingRepository
import ru.vkdev.dressdoll.clothing.api.DollFigure
import ru.vkdev.dressdoll.clothing.api.DollImages
import ru.vkdev.dressdoll.temporarydataset.TemporaryDataset

internal class ClothingRepositoryImpl(
    private val dataset: TemporaryDataset,
) : ClothingRepository {

    override fun clothingCategories(): Flow<List<ClothingCategory>> =
        dataset.clothingCategories().map { categories -> categories.map { it.toClothingCategory() } }

    override fun clothingItems(category: CategoryId): Flow<List<ClothingItem>> =
        dataset.clothingItems(category.toCategoryName()).map { items -> items.map { it.toClothingItem() } }

    override fun allClothingItems(): Flow<List<ClothingItem>> =
        dataset.allClothingItems().map { items -> items.map { it.toClothingItem() } }

    override suspend fun catalogImage(item: ClothingItemId): Bitmap? =
        dataset.catalogImage(item.toTDItemId())

    override suspend fun dollImages(item: ClothingItemId): DollImages =
        dataset.dollImages(item.toTDItemId()).toDollImages()

    override suspend fun figure(): DollFigure = dataset.figure().toDollFigure()
}
