package ru.vkdev.dressdoll.buildlogic.clothing

import com.fasterxml.jackson.core.JsonProcessingException
import java.awt.image.BufferedImage

/** Прямоугольник в пикселях холста куклы. */
internal data class CanvasRect(val left: Int, val top: Int, val width: Int, val height: Int)

/** Левый верхний угол `front.png` и `back.png` вещи на холсте куклы. */
internal data class ItemPlacement(val category: String, val folder: String, val x: Int, val y: Int)

/** [figure] — где на холсте сама фигурка, без прозрачных полей. */
internal data class DollConfig(
    val canvas: ImageSize,
    val figure: CanvasRect,
    val placements: List<ItemPlacement>,
)

/** [config] есть только при пустом [errors]. */
internal class DollValidationResult(val errors: List<String>, val config: DollConfig?)

/**
 * Проверяет папки вещей и их `scale.json` по картинке куклы, правила — в `.docs/doll/doll-canvas.md`.
 * Как и [ClothingDatasetValidator], собирает все нарушения, а не падает на первом.
 */
internal object DollPlacementValidator {

    /** Как в скрипте подготовки одежды: полупрозрачный ореол по краю фигурку не расширяет. */
    const val ALPHA_THRESHOLD = 12

    /**
     * @param figure прямоугольник фигурки, `null` — на картинке куклы нет ни одного непрозрачного пикселя.
     * @param scaleJson текст `scale.json` вещи; вызывается только для вещей, у которых он есть.
     */
    fun validate(
        canvas: ImageSize,
        figure: CanvasRect?,
        items: List<ClothSrcItem>,
        scaleJson: (ClothSrcItem) -> String,
    ): DollValidationResult {
        val errors = mutableListOf<String>()
        if (figure == null) errors += "doll_1.png: нет ни одного пикселя с альфой выше $ALPHA_THRESHOLD"

        val placements = items.mapNotNull { item ->
            if (!item.isFolder) {
                errors += "файл «${item.path}» лежит в папке категории: там должны быть только папки вещей"
                return@mapNotNull null
            }
            checkItem(item, canvas, scaleJson, errors)
        }

        if (errors.isNotEmpty()) return DollValidationResult(errors, config = null)
        return DollValidationResult(emptyList(), DollConfig(canvas, checkNotNull(figure), placements))
    }

    /** Границы пикселей с альфой выше [ALPHA_THRESHOLD]; `null` для полностью прозрачной картинки. */
    fun figureBounds(image: BufferedImage): CanvasRect? {
        var left = image.width
        var top = image.height
        var right = -1
        var bottom = -1
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                if (image.getRGB(x, y) ushr 24 > ALPHA_THRESHOLD) {
                    if (x < left) left = x
                    if (x > right) right = x
                    if (y < top) top = y
                    bottom = y
                }
            }
        }
        if (right < 0) return null
        return CanvasRect(left = left, top = top, width = right - left + 1, height = bottom - top + 1)
    }

    private fun checkItem(
        item: ClothSrcItem,
        canvas: ImageSize,
        scaleJson: (ClothSrcItem) -> String,
        errors: MutableList<String>,
    ): ItemPlacement? {
        val owner = "«${item.path}»"
        val before = errors.size
        listOf(ItemFiles.SCALE, ItemFiles.FRONT, ItemFiles.CATALOG)
            .filterNot { it in item.files }
            .forEach { errors += "$owner: нет $it" }

        val front = sizeOf(item, ItemFiles.FRONT, owner, errors)
        val back = sizeOf(item, ItemFiles.BACK, owner, errors)
        if (front != null && back != null && back != front) {
            errors += "$owner: back.png $back, а front.png $front: плоскости ставятся в одну точку и должны совпадать"
        }
        if (ItemFiles.SCALE !in item.files) return null

        val root = try {
            readJsonTree(scaleJson(item)).root as? Map<*, *>
        } catch (e: JsonProcessingException) {
            errors += "$owner: не удалось разобрать scale.json: ${e.originalMessage}"
            return null
        }
        if (root == null) {
            errors += "$owner: корень scale.json должен быть объектом"
            return null
        }
        val dollCanvas = intPair(root, "doll_canvas", owner, errors)
        val scaledSize = intPair(root, "scaled_size", owner, errors)
        val offset = intPair(root, "offset_on_doll", owner, errors)

        if (dollCanvas != null && (dollCanvas.first != canvas.width || dollCanvas.second != canvas.height)) {
            errors += "$owner: doll_canvas ${dollCanvas.first}×${dollCanvas.second}, а doll_1.png $canvas"
        }
        if (scaledSize != null && front != null && (scaledSize.first != front.width || scaledSize.second != front.height)) {
            errors += "$owner: scaled_size ${scaledSize.first}×${scaledSize.second}, а front.png $front"
        }
        if (offset != null && front != null) {
            val (x, y) = offset
            if (x + front.width <= 0 || y + front.height <= 0 || x >= canvas.width || y >= canvas.height) {
                errors += "$owner: offset_on_doll [$x, $y] уводит вещь $front целиком за холст $canvas"
            }
        }
        if (errors.size != before || offset == null) return null
        return ItemPlacement(item.category, item.folder, x = offset.first, y = offset.second)
    }

    private fun sizeOf(item: ClothSrcItem, file: String, owner: String, errors: MutableList<String>): ImageSize? {
        if (file !in item.files) return null
        val size = item.imageSizes[file]
        if (size == null) errors += "$owner: $file не читается как PNG"
        return size
    }

    private fun intPair(root: Map<*, *>, field: String, owner: String, errors: MutableList<String>): Pair<Int, Int>? {
        val value = root[field]
        val pair = (value as? List<*>)?.takeIf { it.size == 2 && it.all { n -> n is Int } }
        if (pair == null) {
            errors += "$owner: «$field» в scale.json должен быть массивом из двух целых чисел, сейчас «$value»"
            return null
        }
        return pair[0] as Int to pair[1] as Int
    }
}
