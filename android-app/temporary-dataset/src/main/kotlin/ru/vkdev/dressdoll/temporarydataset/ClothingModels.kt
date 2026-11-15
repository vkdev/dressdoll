package ru.vkdev.dressdoll.temporarydataset

import android.graphics.Bitmap

/** Name of a clothing category, equal to its folder name in the dataset (e.g. "сапоги"). */
@JvmInline
value class CategoryName(val value: String)

data class TDClothingCategory(val name: CategoryName)

/** Location of an item in the dataset: `cloth-src/<category>/<folder>/`. */
data class TDItemId(
    val category: CategoryName,
    val folder: String,
)

/**
 * @param id where the item's images live in the dataset.
 * @param name category name + item folder name, e.g. "сапоги 01".
 */
data class TDClothingItem(
    val id: TDItemId,
    val name: String,
) {
    /** Folder name of the category this item belongs to. */
    val parentCategory: CategoryName get() = id.category
}

/** Image files of an item. */
enum class TDImageKind(val fileName: String) {
    /** Whole item for the catalog, including neckline and paper tabs. */
    CATALOG("catalog.png"),

    /** Front plane of the item worn on the doll. */
    FRONT("front.png"),

    /** Back plane of the item worn on the doll, hidden behind the figure. Optional. */
    BACK("back.png"),
}

/** Images to draw an item on the doll. [back] is null when the item has no back plane. */
data class TDDollImages(
    val front: Bitmap,
    val back: Bitmap?,
)
