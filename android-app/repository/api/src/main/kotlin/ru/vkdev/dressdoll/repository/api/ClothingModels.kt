package ru.vkdev.dressdoll.repository.api

import android.graphics.Bitmap

@JvmInline
value class CategoryId(val value: String)

/** [title] — название для пользователя, например «сапоги». */
data class ClothingCategory(
    val id: CategoryId,
    val title: String,
)

/** [key] уникален только внутри [category]. */
data class ClothingItemId(
    val category: CategoryId,
    val key: String,
)

/** [title] — название для пользователя, например «сапоги 01». */
data class ClothingItem(
    val id: ClothingItemId,
    val title: String,
) {
    val category: CategoryId get() = id.category
}

/** [back] — `null`, если у вещи нет задней плоскости. */
data class DollImages(
    val front: Bitmap,
    val back: Bitmap?,
)
