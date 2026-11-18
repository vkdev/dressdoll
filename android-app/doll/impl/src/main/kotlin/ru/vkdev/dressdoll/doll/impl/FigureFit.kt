package ru.vkdev.dressdoll.doll.impl

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import ru.vkdev.dressdoll.clothing.api.CanvasRect
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

/** Фигурка в координатах области. Пока кукла не помещается — пустой прямоугольник в середине области над шторкой. */
internal fun figureRect(bounds: CanvasRect?, areaWidth: Float, availableHeight: Float, margin: Float): Rect {
    val fit = bounds?.let { fitFigure(it, areaWidth, availableHeight, margin) }
    if (bounds == null || fit == null || fit.scale <= 0f) return Rect(Offset(areaWidth / 2, availableHeight / 2), Size.Zero)
    return Rect(
        left = fit.translationX + fit.scale * bounds.left,
        top = fit.translationY + fit.scale * bounds.top,
        right = fit.translationX + fit.scale * (bounds.left + bounds.width),
        bottom = fit.translationY + fit.scale * (bounds.top + bounds.height),
    )
}
