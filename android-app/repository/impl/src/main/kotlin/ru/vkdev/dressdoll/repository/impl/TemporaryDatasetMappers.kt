package ru.vkdev.dressdoll.repository.impl

import ru.vkdev.dressdoll.repository.api.CategoryId
import ru.vkdev.dressdoll.repository.api.ClothingCategory
import ru.vkdev.dressdoll.repository.api.ClothingItem
import ru.vkdev.dressdoll.repository.api.ClothingItemId
import ru.vkdev.dressdoll.repository.api.DollImages
import ru.vkdev.dressdoll.temporarydataset.CategoryName
import ru.vkdev.dressdoll.temporarydataset.TDClothingCategory
import ru.vkdev.dressdoll.temporarydataset.TDClothingItem
import ru.vkdev.dressdoll.temporarydataset.TDDollImages
import ru.vkdev.dressdoll.temporarydataset.TDItemId

// Dataset -> repository

internal fun CategoryName.toCategoryId(): CategoryId = CategoryId(value)

internal fun TDClothingCategory.toClothingCategory(): ClothingCategory =
    ClothingCategory(id = name.toCategoryId(), title = name.value)

internal fun TDItemId.toClothingItemId(): ClothingItemId =
    ClothingItemId(category = category.toCategoryId(), key = folder)

internal fun TDClothingItem.toClothingItem(): ClothingItem =
    ClothingItem(id = id.toClothingItemId(), title = name)

internal fun TDDollImages.toDollImages(): DollImages = DollImages(front = front, back = back)

// Repository -> dataset

internal fun CategoryId.toCategoryName(): CategoryName = CategoryName(value)

internal fun ClothingItemId.toTDItemId(): TDItemId =
    TDItemId(category = category.toCategoryName(), folder = key)
