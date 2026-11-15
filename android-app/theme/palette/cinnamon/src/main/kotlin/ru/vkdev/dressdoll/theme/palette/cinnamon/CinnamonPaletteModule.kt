package ru.vkdev.dressdoll.theme.palette.cinnamon

import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.vkdev.dressdoll.theme.api.ThemeIds
import ru.vkdev.dressdoll.theme.api.ThemePalette

val cinnamonPaletteModule = module {
    single<ThemePalette>(named(ThemeIds.CINNAMON)) {
        ThemePalette(
            id = ThemeIds.CINNAMON,
            titleRes = R.string.theme_palette_cinnamon_title,
            order = 0,
            colorScheme = CinnamonColors,
            typography = CinnamonTypography,
        )
    }
}
