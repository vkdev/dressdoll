package ru.vkdev.dressdoll.doll.impl

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import ru.vkdev.dressdoll.repository.api.CanvasPoint
import ru.vkdev.dressdoll.theme.api.Spacing

/**
 * Кукла, вписанная по прямоугольнику фигурки в область шириной с ограничения раскладки
 * и высотой [availableHeight] пикселей от верха, с полями [Spacing.overhang]. Одежда может
 * выходить за область: обрезает вызывающий. `scene == null` — пустая область, образ ещё читается.
 *
 * [availableHeight] — лямбда: она читается при отрисовке слоя, поэтому перетаскивание шторки
 * не вызывает ни рекомпозиции, ни перекомпоновки.
 */
@Composable
internal fun DollCanvas(scene: DollScene?, availableHeight: () -> Float, modifier: Modifier = Modifier) {
    Layout(
        content = { if (scene != null) DollLayers(scene) },
        modifier = modifier,
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val overhang = Spacing.overhang.toPx()
        val canvas = scene?.let {
            measurables.single().measure(Constraints.fixed(it.figure.canvasWidth, it.figure.canvasHeight))
        }
        layout(width, constraints.minHeight) {
            if (scene == null || canvas == null) return@layout
            // Масштаб при отрисовке, а не растеризацией слоя заранее: увеличенные картинки
            // фильтруются в итоговом разрешении. Поэтому у этого слоя нельзя alpha < 1 и Offscreen.
            canvas.placeWithLayer(0, 0) {
                val fit = fitFigure(scene.figure.bounds, width.toFloat(), availableHeight(), overhang)
                // С началом в левом верхнем углу сдвиг считается прямо в пикселях холста.
                transformOrigin = TransformOrigin(0f, 0f)
                scaleX = fit.scale
                scaleY = fit.scale
                translationX = fit.translationX
                translationY = fit.translationY
            }
        }
    }
}

/** Слои в пикселях холста: каждый своего размера в своей точке, задние под фигуркой, передние над ней. */
@Composable
private fun DollLayers(scene: DollScene) {
    val figure = scene.figure
    val figureImage = remember(figure.image) { figure.image.asImageBitmap() }
    val (backs, fronts) = scene.layers.partition { it.plane == Plane.BACK }
    val images = backs.map { it.toCanvasImage() } +
        CanvasImage(FigureKey, figureImage, CanvasPoint(0, 0)) +
        fronts.map { it.toCanvasImage() }
    Layout(
        content = {
            images.forEach { image ->
                key(image.key) {
                    Image(bitmap = image.bitmap, contentDescription = null, contentScale = ContentScale.FillBounds)
                }
            }
        },
    ) { measurables, _ ->
        val placeables = measurables.mapIndexed { index, measurable ->
            val bitmap = images[index].bitmap
            measurable.measure(Constraints.fixed(bitmap.width, bitmap.height))
        }
        layout(figure.canvasWidth, figure.canvasHeight) {
            placeables.forEachIndexed { index, placeable ->
                placeable.place(images[index].topLeft.x, images[index].topLeft.y)
            }
        }
    }
}

/** [key] стабилен между сценами: надетая вещь не пересоздаёт свои слои при смене образа. */
private class CanvasImage(val key: Any, val bitmap: ImageBitmap, val topLeft: CanvasPoint)

private data object FigureKey

private fun DollLayer.toCanvasImage() = CanvasImage(key = item to plane, bitmap = image, topLeft = topLeft)
