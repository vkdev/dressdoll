package ru.vkdev.dressdoll.theme.impl

import org.junit.Assert.assertEquals
import org.junit.Test
import org.koin.dsl.koinApplication
import ru.vkdev.dressdoll.theme.api.SystemDefault
import ru.vkdev.dressdoll.theme.api.ThemePalette
import ru.vkdev.dressdoll.theme.palette.avocado.avocadoPaletteModule
import ru.vkdev.dressdoll.theme.palette.cinnamon.cinnamonPaletteModule
import ru.vkdev.dressdoll.theme.palette.denim.denimPaletteModule
import ru.vkdev.dressdoll.theme.palette.disco.discoPaletteModule
import ru.vkdev.dressdoll.theme.palette.powder.powderPaletteModule

// Список палитр повторяет :app: роли проверяются на наборе, который приложение реально собирает.
class PaletteRolesTest {

    @Test
    fun `exactly one light and one dark palette`() {
        val koin = koinApplication {
            modules(
                cinnamonPaletteModule,
                discoPaletteModule,
                avocadoPaletteModule,
                powderPaletteModule,
                denimPaletteModule,
            )
        }
        val palettes = try {
            koin.koin.getAll<ThemePalette>()
        } finally {
            koin.close()
        }

        assertEquals(5, palettes.size)
        assertEquals(1, palettes.count { it.systemDefault == SystemDefault.LIGHT })
        assertEquals(1, palettes.count { it.systemDefault == SystemDefault.DARK })
    }
}
