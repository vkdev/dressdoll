package ru.vkdev.dressdoll.theme.palette.powder

import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.vkdev.dressdoll.theme.api.ThemePalette

const val POWDER_ID = "powder"

val powderPaletteModule = module {
    single<ThemePalette>(named(POWDER_ID)) {
        ThemePalette(
            id = POWDER_ID,
            titleRes = R.string.theme_palette_powder_title,
            order = 3,
            colorScheme = PowderColors,
            typography = PowderTypography,
        )
    }
}
