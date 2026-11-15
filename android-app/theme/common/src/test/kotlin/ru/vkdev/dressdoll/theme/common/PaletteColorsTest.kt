package ru.vkdev.dressdoll.theme.common

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PaletteColorsTest {

    private val light = PaletteBaseColors(
        primary = Color(0xFFB5532E),
        onPrimary = Color(0xFFFFFFFF),
        secondary = Color(0xFFD9A23A),
        tertiary = Color(0xFF6F7140),
        background = Color(0xFFF3E6D0),
        surface = Color(0xFFFBF4E8),
        surfaceVariant = Color(0xFFE8D3B4),
        onSurface = Color(0xFF3A2414),
        outline = Color(0xFFA88C70),
    )

    private val dark = PaletteBaseColors(
        primary = Color(0xFFE0559A),
        onPrimary = Color(0xFF24122E),
        secondary = Color(0xFFD4A84B),
        tertiary = Color(0xFFB9A6D6),
        background = Color(0xFF24122E),
        surface = Color(0xFF321C3F),
        surfaceVariant = Color(0xFF43284F),
        onSurface = Color(0xFFF6E9F0),
        outline = Color(0xFF7D6589),
    )

    @Test
    fun `base colors are kept as is`() {
        listOf(light to false, dark to true).forEach { (base, isDark) ->
            val scheme = paletteColorScheme(base, isDark)
            assertEquals(base.primary, scheme.primary)
            assertEquals(base.onPrimary, scheme.onPrimary)
            assertEquals(base.secondary, scheme.secondary)
            assertEquals(base.tertiary, scheme.tertiary)
            assertEquals(base.background, scheme.background)
            assertEquals(base.surface, scheme.surface)
            assertEquals(base.surfaceVariant, scheme.surfaceVariant)
            assertEquals(base.onSurface, scheme.onSurface)
            assertEquals(base.outline, scheme.outline)
            assertEquals(base.onSurface, scheme.onBackground)
        }
    }

    @Test
    fun `text on containers is readable`() {
        listOf(light to false, dark to true).forEach { (base, isDark) ->
            val s = paletteColorScheme(base, isDark)
            assertAtLeast(CONTAINER_CONTRAST, s.onPrimaryContainer, s.primaryContainer)
            assertAtLeast(CONTAINER_CONTRAST, s.onSecondaryContainer, s.secondaryContainer)
            assertAtLeast(CONTAINER_CONTRAST, s.onTertiaryContainer, s.tertiaryContainer)
            assertAtLeast(VARIANT_CONTRAST, s.onSurfaceVariant, s.surfaceVariant)
        }
    }

    @Test
    fun `on colors pick the more contrasting option`() {
        val lightScheme = paletteColorScheme(light, dark = false)
        assertEquals(light.onSurface, lightScheme.onSecondary)
        assertEquals(Color.White, lightScheme.onTertiary)

        val darkScheme = paletteColorScheme(dark, dark = true)
        assertEquals(dark.background, darkScheme.onSecondary)
    }

    @Test
    fun `surface containers step from surface to surface variant`() {
        val s = paletteColorScheme(light, dark = false)
        val steps = listOf(s.surfaceContainerLow, s.surfaceContainer, s.surfaceContainerHigh, s.surfaceContainerHighest)
        val distances = steps.map { contrast(it, light.surface) }
        assertEquals(distances.sorted(), distances)
        assertEquals(light.surface, s.surfaceContainerLow)
    }

    private fun assertAtLeast(min: Float, text: Color, background: Color) {
        val actual = contrast(text, background)
        assertTrue("contrast $actual < $min for $text on $background", actual >= min)
    }
}
