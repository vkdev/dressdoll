package ru.vkdev.dressdoll.clothing.api

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
)

/**
 * Часть тела, к которой относится вещь. Позицию вещи на кукле задаёт не слот, а [DollImages.topLeft];
 * слот — задел для креплений и аксессуаров: брошь крепится к вещи торса, шарф — к шее.
 */
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

/**
 * Уровень с большей [position] рисуется поверх. Позиция задаёт только порядок, это не z-index:
 * его считает кукла с учётом порядка надевания.
 */
data class DrawLevel(val id: DrawLevelId, val position: Int) {

    fun compareByPosition(other: DrawLevel): Int = position.compareTo(other.position)
}

/** Одновременно надевается не больше [limit] вещей из категорий группы. */
data class ReplacementGroup(val id: ReplacementGroupId, val limit: Int)

/** [key] уникален только внутри [category]. */
data class ClothingItemId(
    val category: CategoryId,
    val key: String,
)

/** [title] — название для пользователя, например «сапоги 01». */
data class ClothingItem(
    val id: ClothingItemId,
    val title: String,
)

/** Координаты в пикселях холста куклы. */
data class CanvasPoint(val x: Int, val y: Int)

data class CanvasRect(val left: Int, val top: Int, val width: Int, val height: Int)

/** [image] — холст куклы целиком, размером [canvasWidth] × [canvasHeight]. */
data class DollFigure(
    val image: Bitmap,
    val canvasWidth: Int,
    val canvasHeight: Int,
    /** Где на холсте сама фигурка: по ней считается масштаб. */
    val bounds: CanvasRect,
)

/**
 * [back] — `null`, если у вещи нет задней плоскости.
 * [topLeft] — левый верхний угол [front] и [back] на холсте куклы.
 */
data class DollImages(
    val front: Bitmap,
    val back: Bitmap?,
    val topLeft: CanvasPoint,
)
