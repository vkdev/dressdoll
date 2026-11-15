package ru.vkdev.dressdoll.temporarydataset

/** Name of a clothing category, equal to its folder name in the dataset (e.g. "сапоги"). */
@JvmInline
value class CategoryName(val value: String)

data class TDClothingCategory(val name: CategoryName)

/**
 * @param name category name + item folder name, e.g. "сапоги 01".
 * @param parentCategory folder name of the category this item belongs to.
 */
data class TDClothingItem(
    val name: String,
    val parentCategory: CategoryName,
)
