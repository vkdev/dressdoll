package ru.vkdev.dressdoll.theme.palette.avocado

import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.vkdev.dressdoll.theme.api.ThemePalette

const val AVOCADO_ID = "avocado"

val avocadoPaletteModule = module {
    single<ThemePalette>(named(AVOCADO_ID)) {
        ThemePalette(
            id = AVOCADO_ID,
            titleRes = R.string.theme_palette_avocado_title,
            order = 2,
            colorScheme = AvocadoColors,
            typography = AvocadoTypography,
        )
    }
}
