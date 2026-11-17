package ru.vkdev.dressdoll.doll.impl

import ru.vkdev.dressdoll.repository.api.CanvasRect
import kotlin.math.max
import kotlin.math.min

/** Экранная точка = `translation + scale · точка холста`, начало преобразования — левый верхний угол холста. */
internal data class FigureFit(val scale: Float, val translationX: Float, val translationY: Float)

/**
 * Вписывает прямоугольник фигурки [bounds] в область [areaWidth] × [areaHeight] с полями [margin]
 * со всех сторон, как `ContentScale.Fit`, и ставит его центр в центр области.
 */
internal fun fitFigure(bounds: CanvasRect, areaWidth: Float, areaHeight: Float, margin: Float): FigureFit {
    val innerWidth = max(areaWidth - 2 * margin, 0f)
    val innerHeight = max(areaHeight - 2 * margin, 0f)
    val scale = min(innerWidth / bounds.width, innerHeight / bounds.height)
    val centerX = bounds.left + bounds.width / 2f
    val centerY = bounds.top + bounds.height / 2f
    return FigureFit(
        scale = scale,
        translationX = areaWidth / 2f - centerX * scale,
        translationY = areaHeight / 2f - centerY * scale,
    )
}
