package ru.vkdev.dressdoll.repository.api

import android.graphics.Bitmap

/** Stable identifier of a clothing category. */
@JvmInline
value class CategoryId(val value: String)

/**
 * @param id identifier to request the category's items.
 * @param title human-readable category name, e.g. "сапоги".
 */
data class ClothingCategory(
    val id: CategoryId,
    val title: String,
)

/** Stable identifier of a clothing item within its [category]. */
data class ClothingItemId(
    val category: CategoryId,
    val key: String,
)

/**
 * @param id identifier to request the item's images.
 * @param title human-readable item name, e.g. "сапоги 01".
 */
data class ClothingItem(
    val id: ClothingItemId,
    val title: String,
) {
    val category: CategoryId get() = id.category
}

/** Images to draw an item on the doll. [back] is null when the item has no back plane. */
data class DollImages(
    val front: Bitmap,
    val back: Bitmap?,
)
