package ru.vkdev.dressdoll.doll.impl

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import ru.vkdev.dressdoll.clothing.api.CanvasPoint
import ru.vkdev.dressdoll.clothing.api.CanvasRect
import ru.vkdev.dressdoll.clothing.api.ClothingItemId
import ru.vkdev.dressdoll.theme.ui.Spacing

/**
 * Одежда может выходить за область: обрезает вызывающий.
 *
 * [availableHeight] — лямбда: она читается при отрисовке слоя, поэтому перетаскивание шторки
 * не вызывает ни рекомпозиции, ни перекомпоновки.
 *
 * Вещи летят из-за краёв всей области по высоте ограничений, а не только [availableHeight]:
 * снизу они выезжают из-под шторки.
 */
@Composable
internal fun DollCanvas(
    scene: DollScene?,
    transition: DollTransition,
    availableHeight: () -> Float,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(scene) { transition.show(scene) }
    // Первая сцена рисуется сразу, а не через кадр, когда до неё доберётся эффект.
    val shown = transition.shown ?: scene
    // Размер области нужен слоям при отрисовке; это не состояние: пока ничего не летит, он не читается,
    // а в полёте слои и так перерисовываются каждый кадр.
    val area = remember { AreaSize() }
    val overhang = with(LocalDensity.current) { Spacing.overhang.toPx() }
    val viewport = { shown?.figure?.let { area.viewport(it.bounds, availableHeight(), overhang) } }
    Layout(
        content = { if (shown != null) DollLayers(shown, transition, viewport) },
        modifier = modifier,
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        area.width = width.toFloat()
        area.height = constraints.minHeight.toFloat()
        val canvas = shown?.let {
            measurables.single().measure(Constraints.fixed(it.figure.canvasWidth, it.figure.canvasHeight))
        }
        layout(width, constraints.minHeight) {
            if (shown == null || canvas == null) return@layout
            // Масштаб при отрисовке, а не растеризацией слоя заранее: увеличенные картинки
            // фильтруются в итоговом разрешении. Поэтому у этого слоя нельзя alpha < 1 и Offscreen.
            canvas.placeWithLayer(0, 0) {
                val fit = fitFigure(shown.figure.bounds, width.toFloat(), availableHeight(), overhang)
                transformOrigin = TransformOrigin(0f, 0f)
                scaleX = fit.scale
                scaleY = fit.scale
                translationX = fit.translationX
                translationY = fit.translationY
            }
        }
    }
}

@Composable
private fun DollLayers(scene: DollScene, transition: DollTransition, viewport: () -> Rect?) {
    val figure = scene.figure
    val figureImage = remember(figure.image) { figure.image.asImageBitmap() }
    val (backs, fronts) = scene.layers.partition { it.plane == Plane.BACK }
    val images = backs.map { it.toCanvasImage() } +
        CanvasImage(FigureKey, figureImage, CanvasPoint(0, 0), item = null) +
        fronts.map { it.toCanvasImage() } +
        transition.leaving.map { it.toCanvasImage() }
    val flights = transition.flights
    Layout(
        content = {
            images.forEach { image ->
                key(image.key) {
                    val flight = image.item?.let(flights::get)
                    Image(
                        bitmap = image.bitmap,
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = if (flight == null) Modifier else Modifier.graphicsLayer {
                            val area = viewport() ?: return@graphicsLayer
                            val offset = flight.offset(transition.progress.value, image.rect, area)
                            translationX = offset.x
                            translationY = offset.y
                        },
                    )
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
private class CanvasImage(val key: Any, val bitmap: ImageBitmap, val topLeft: CanvasPoint, val item: ClothingItemId?) {
    val rect: Rect
        get() = Rect(Offset(topLeft.x.toFloat(), topLeft.y.toFloat()), Size(bitmap.width.toFloat(), bitmap.height.toFloat()))
}

private data object FigureKey

private fun DollLayer.toCanvasImage() = CanvasImage(key = item to plane, bitmap = image, topLeft = topLeft, item = item)

private class AreaSize {
    var width = 0f
    var height = 0f

    /** `null`, пока кукла не помещается и масштаб нулевой. */
    fun viewport(bounds: CanvasRect, availableHeight: Float, overhang: Float): Rect? {
        val fit = fitFigure(bounds, width, availableHeight, overhang)
        if (fit.scale <= 0f) return null
        return Rect(
            left = -fit.translationX / fit.scale,
            top = -fit.translationY / fit.scale,
            right = (width - fit.translationX) / fit.scale,
            bottom = (height - fit.translationY) / fit.scale,
        )
    }
}
