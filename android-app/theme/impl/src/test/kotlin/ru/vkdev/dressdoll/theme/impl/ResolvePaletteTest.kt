package ru.vkdev.dressdoll.theme.impl

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.vkdev.dressdoll.theme.api.ThemeChoice
import ru.vkdev.dressdoll.theme.api.ThemeIds

class ResolvePaletteTest {

    private val cinnamon = testPalette(ThemeIds.CINNAMON)
    private val disco = testPalette(ThemeIds.DISCO)
    private val denim = testPalette("denim")
    private val all = listOf(cinnamon, disco, denim)

    @Test
    fun `system choice follows system dark mode`() {
        assertEquals(disco, resolvePalette(ThemeChoice.System, all, systemDark = true))
        assertEquals(cinnamon, resolvePalette(ThemeChoice.System, all, systemDark = false))
    }

    @Test
    fun `explicit palette ignores system dark mode`() {
        assertEquals(denim, resolvePalette(ThemeChoice.Palette("denim"), all, systemDark = true))
        assertEquals(disco, resolvePalette(ThemeChoice.Palette(ThemeIds.DISCO), all, systemDark = false))
    }

    @Test
    fun `unknown palette falls back to first available`() {
        assertEquals(cinnamon, resolvePalette(ThemeChoice.Palette("missing"), all, systemDark = false))
        assertEquals(denim, resolvePalette(ThemeChoice.System, listOf(denim), systemDark = true))
    }

    @Test
    fun `no palettes means default material scheme`() {
        assertNull(resolvePalette(ThemeChoice.System, emptyList(), systemDark = false))
    }
}
