package ru.vkdev.dressdoll.theme.palette.denim

import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.vkdev.dressdoll.theme.api.ThemePalette

const val DENIM_ID = "denim"

val denimPaletteModule = module {
    single<ThemePalette>(named(DENIM_ID)) {
        ThemePalette(
            id = DENIM_ID,
            titleRes = R.string.theme_palette_denim_title,
            taglineRes = R.string.theme_palette_denim_tagline,
            order = 4,
            colorScheme = DenimColors,
            typography = DenimTypography,
            uppercaseHeadlines = true,
        )
    }
}
