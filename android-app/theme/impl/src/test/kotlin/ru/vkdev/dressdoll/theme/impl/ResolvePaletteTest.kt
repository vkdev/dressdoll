package ru.vkdev.dressdoll.theme.impl

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.vkdev.dressdoll.theme.api.SystemDefault
import ru.vkdev.dressdoll.theme.api.ThemeChoice

class ResolvePaletteTest {

    private val cinnamon = testPalette("cinnamon", systemDefault = SystemDefault.LIGHT)
    private val disco = testPalette("disco", systemDefault = SystemDefault.DARK)
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
        assertEquals(disco, resolvePalette(ThemeChoice.Palette("disco"), all, systemDark = false))
    }

    @Test
    fun `unknown palette falls back to first available`() {
        assertEquals(cinnamon, resolvePalette(ThemeChoice.Palette("missing"), all, systemDark = false))
        assertEquals(denim, resolvePalette(ThemeChoice.System, listOf(denim), systemDark = true))
    }

    @Test
    fun `system choice without role palette falls back to first available`() {
        val plain = listOf(denim, testPalette("powder"))

        assertEquals(denim, resolvePalette(ThemeChoice.System, plain, systemDark = false))
    }

    @Test
    fun `no palettes means default material scheme`() {
        assertNull(resolvePalette(ThemeChoice.System, emptyList(), systemDark = false))
    }
}
