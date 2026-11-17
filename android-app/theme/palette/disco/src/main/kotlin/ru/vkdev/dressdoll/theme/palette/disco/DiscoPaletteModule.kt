package ru.vkdev.dressdoll.theme.palette.disco

import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.vkdev.dressdoll.theme.api.ThemeIds
import ru.vkdev.dressdoll.theme.api.ThemePalette

val discoPaletteModule = module {
    single<ThemePalette>(named(ThemeIds.DISCO)) {
        ThemePalette(
            id = ThemeIds.DISCO,
            titleRes = R.string.theme_palette_disco_title,
            taglineRes = R.string.theme_palette_disco_tagline,
            order = 1,
            colorScheme = DiscoColors,
            typography = DiscoTypography,
        )
    }
}
