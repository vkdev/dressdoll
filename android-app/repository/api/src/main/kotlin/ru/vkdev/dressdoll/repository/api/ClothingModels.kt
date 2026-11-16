package ru.vkdev.dressdoll.repository.api

import android.graphics.Bitmap

@JvmInline
value class CategoryId(val value: String)

/**
 * [title] — название для пользователя, например «сапоги».
 *
 * [slot], [level] и [group] — внутренние правила надевания, пользователь их не видит.
 * Правила описаны в `.docs/doll/z-index.md`.
 */
data class ClothingCategory(
    val id: CategoryId,
    val title: String,
    val slot: BodySlot,
    val level: DrawLevel,
    val group: ReplacementGroup,
    val section: CatalogSectionId,
)

/** Якорная точка вещи на фигурке. */
enum class BodySlot {
    FEET,
    LEGS,
    WAIST,
    TORSO,
    NECK,
    ARMS,
    HEAD,
}

@JvmInline
value class DrawLevelId(val value: String)

@JvmInline
value class ReplacementGroupId(val value: String)

@JvmInline
value class CatalogSectionId(val value: String)

/**
 * Уровень с большей [position] рисуется поверх. Позиция задаёт только порядок, это не z-index:
 * его считает кукла с учётом порядка надевания.
 */
data class DrawLevel(val id: DrawLevelId, val position: Int) {

    fun compareByPosition(other: DrawLevel): Int = position.compareTo(other.position)
}

/** Одновременно надевается не больше [limit] вещей из категорий группы. */
data class ReplacementGroup(val id: ReplacementGroupId, val limit: Int)

/** Раздел каталога, [title] видит пользователь. */
data class CatalogSection(val id: CatalogSectionId, val title: String)

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
