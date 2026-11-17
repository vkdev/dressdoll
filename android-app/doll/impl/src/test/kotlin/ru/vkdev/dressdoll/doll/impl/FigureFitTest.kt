package ru.vkdev.dressdoll.doll.impl

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.vkdev.dressdoll.repository.api.CanvasRect

class FigureFitTest {

    private val bounds = CanvasRect(left = 221, top = 66, width = 329, height = 1226)

    @Test
    fun `высокая область — фигурка вписана по ширине`() {
        val fit = fitFigure(bounds, areaWidth = 389f, areaHeight = 3000f, margin = 30f)

        assertEquals(1f, fit.scale, EPS)
        assertFigureAt(fit, left = 30f, top = (3000f - 1226f) / 2, right = 359f)
    }

    @Test
    fun `низкая область — фигурка вписана по высоте с полями сверху и снизу`() {
        val fit = fitFigure(bounds, areaWidth = 1000f, areaHeight = 673f, margin = 30f)

        assertEquals(0.5f, fit.scale, EPS)
        val top = fit.translationY + bounds.top * fit.scale
        val bottom = fit.translationY + (bounds.top + bounds.height) * fit.scale
        assertEquals(30f, top, EPS)
        assertEquals(643f, bottom, EPS)
        assertEquals(500f, fit.translationX + (bounds.left + bounds.width / 2f) * fit.scale, EPS)
    }

    @Test
    fun `область меньше полей — масштаб ноль, а не отрицательный`() {
        assertEquals(0f, fitFigure(bounds, areaWidth = 40f, areaHeight = 40f, margin = 30f).scale, EPS)
    }

    private fun assertFigureAt(fit: FigureFit, left: Float, top: Float, right: Float) {
        assertEquals(left, fit.translationX + bounds.left * fit.scale, EPS)
        assertEquals(right, fit.translationX + (bounds.left + bounds.width) * fit.scale, EPS)
        assertEquals(top, fit.translationY + bounds.top * fit.scale, EPS)
    }

    private companion object {
        const val EPS = 1e-3f
    }
}
