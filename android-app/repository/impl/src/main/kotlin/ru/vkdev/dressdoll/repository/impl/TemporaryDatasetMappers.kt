package ru.vkdev.dressdoll.repository.impl

import ru.vkdev.dressdoll.repository.api.BodySlot
import ru.vkdev.dressdoll.repository.api.CanvasPoint
import ru.vkdev.dressdoll.repository.api.CanvasRect
import ru.vkdev.dressdoll.repository.api.CatalogSection
import ru.vkdev.dressdoll.repository.api.CatalogSectionId
import ru.vkdev.dressdoll.repository.api.CategoryId
import ru.vkdev.dressdoll.repository.api.ClothingCategory
import ru.vkdev.dressdoll.repository.api.ClothingItem
import ru.vkdev.dressdoll.repository.api.ClothingItemId
import ru.vkdev.dressdoll.repository.api.DollFigure
import ru.vkdev.dressdoll.repository.api.DollImages
import ru.vkdev.dressdoll.repository.api.DrawLevel
import ru.vkdev.dressdoll.repository.api.DrawLevelId
import ru.vkdev.dressdoll.repository.api.ReplacementGroup
import ru.vkdev.dressdoll.repository.api.ReplacementGroupId
import ru.vkdev.dressdoll.temporarydataset.CategoryName
import ru.vkdev.dressdoll.temporarydataset.TDCanvasPoint
import ru.vkdev.dressdoll.temporarydataset.TDCanvasRect
import ru.vkdev.dressdoll.temporarydataset.TDClothingCategory
import ru.vkdev.dressdoll.temporarydataset.TDClothingItem
import ru.vkdev.dressdoll.temporarydataset.TDDollFigure
import ru.vkdev.dressdoll.temporarydataset.TDDollImages
import ru.vkdev.dressdoll.temporarydataset.TDGroup
import ru.vkdev.dressdoll.temporarydataset.TDItemId
import ru.vkdev.dressdoll.temporarydataset.TDLevel
import ru.vkdev.dressdoll.temporarydataset.TDSection
import ru.vkdev.dressdoll.temporarydataset.TDSectionId
import ru.vkdev.dressdoll.temporarydataset.TDSlot

internal fun CategoryName.toCategoryId(): CategoryId = CategoryId(value)

internal fun TDClothingCategory.toClothingCategory(): ClothingCategory =
    ClothingCategory(
        id = name.toCategoryId(),
        title = name.value,
        slot = slot.toBodySlot(),
        level = level.toDrawLevel(),
        group = group.toReplacementGroup(),
        section = section.toCatalogSectionId(),
    )

// when без else: новый TDSlot без маппинга не скомпилируется.
internal fun TDSlot.toBodySlot(): BodySlot = when (this) {
    TDSlot.FEET -> BodySlot.FEET
    TDSlot.LEGS -> BodySlot.LEGS
    TDSlot.WAIST -> BodySlot.WAIST
    TDSlot.TORSO -> BodySlot.TORSO
    TDSlot.NECK -> BodySlot.NECK
    TDSlot.ARMS -> BodySlot.ARMS
    TDSlot.HEAD -> BodySlot.HEAD
}

internal fun TDLevel.toDrawLevel(): DrawLevel = DrawLevel(id = DrawLevelId(id.value), position = position)

internal fun TDGroup.toReplacementGroup(): ReplacementGroup =
    ReplacementGroup(id = ReplacementGroupId(id.value), limit = limit)

internal fun TDSectionId.toCatalogSectionId(): CatalogSectionId = CatalogSectionId(value)

internal fun TDSection.toCatalogSection(): CatalogSection =
    CatalogSection(id = id.toCatalogSectionId(), title = title)

internal fun TDItemId.toClothingItemId(): ClothingItemId =
    ClothingItemId(category = category.toCategoryId(), key = folder)

internal fun TDClothingItem.toClothingItem(): ClothingItem =
    ClothingItem(id = id.toClothingItemId(), title = name)

internal fun TDDollImages.toDollImages(): DollImages =
    DollImages(front = front, back = back, topLeft = topLeft.toCanvasPoint())

internal fun TDCanvasPoint.toCanvasPoint(): CanvasPoint = CanvasPoint(x = x, y = y)

internal fun TDCanvasRect.toCanvasRect(): CanvasRect = CanvasRect(left = left, top = top, width = width, height = height)

internal fun TDDollFigure.toDollFigure(): DollFigure =
    DollFigure(image = image, canvasWidth = canvasWidth, canvasHeight = canvasHeight, bounds = bounds.toCanvasRect())

internal fun CategoryId.toCategoryName(): CategoryName = CategoryName(value)

internal fun ClothingItemId.toTDItemId(): TDItemId =
    TDItemId(category = category.toCategoryName(), folder = key)
