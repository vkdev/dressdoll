package ru.vkdev.dressdoll.temporarydataset

import android.graphics.Bitmap

/** Совпадает с именем папки категории в датасете, например «сапоги». */
@JvmInline
value class CategoryName(val value: String)

/**
 * Генерируется при сборке из `categories.json` (`categoriesConfig`).
 * [groups] — только явные группы, неявные есть только у категорий.
 * [categories] упорядочены по разделам, внутри раздела — как в файле.
 */
internal data class TDCategoriesConfig(
    val levels: List<TDLevel>,
    val groups: List<TDGroup>,
    val sections: List<TDSection>,
    val categories: List<TDClothingCategory>,
)

/**
 * Свойства категории из `categories.json`, правила — в `.docs/doll/z-index.md`.
 * Категория без явной группы получает собственную [group] с лимитом 1.
 */
data class TDClothingCategory(
    val name: CategoryName,
    val slot: TDSlot,
    val level: TDLevel,
    val group: TDGroup,
    val section: TDSectionId,
)

/** Имя константы — значение слота в `categories.json` в верхнем регистре: на него ссылается сгенерированный код. */
enum class TDSlot {
    FEET,
    LEGS,
    WAIST,
    TORSO,
    NECK,
    ARMS,
    HEAD,
}

@JvmInline
value class TDLevelId(val value: String)

@JvmInline
value class TDGroupId(val value: String)

@JvmInline
value class TDSectionId(val value: String)

/** [position] считается с 1 от фигурки наружу: уровень выше рисуется поверх. */
data class TDLevel(val id: TDLevelId, val position: Int)

/** Одновременно надевается не больше [limit] вещей из категорий группы. */
data class TDGroup(val id: TDGroupId, val limit: Int)

data class TDSection(val id: TDSectionId, val title: String)

/** Папка вещи в датасете: `cloth-src/<category>/<folder>/`. */
data class TDItemId(
    val category: CategoryName,
    val folder: String,
)

/** [name] — имя категории и папки вещи, например «сапоги 01». */
data class TDClothingItem(
    val id: TDItemId,
    val name: String,
) {
    val parentCategory: CategoryName get() = id.category
}

enum class TDImageKind(val fileName: String) {
    /** Вся вещь для каталога, с горловиной и бумажными клапанами. */
    CATALOG("catalog.png"),

    /** Передняя плоскость вещи на кукле. */
    FRONT("front.png"),

    /** Задняя плоскость, прячется за фигуркой. Есть не у всех вещей. */
    BACK("back.png"),
}

/** [back] — `null`, если у вещи нет задней плоскости. */
data class TDDollImages(
    val front: Bitmap,
    val back: Bitmap?,
)
