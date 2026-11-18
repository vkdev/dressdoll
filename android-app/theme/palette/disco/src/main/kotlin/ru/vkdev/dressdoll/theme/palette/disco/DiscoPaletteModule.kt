package ru.vkdev.dressdoll.theme.palette.disco

import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.vkdev.dressdoll.theme.api.SystemDefault
import ru.vkdev.dressdoll.theme.api.ThemePalette

const val DISCO_ID = "disco"

val discoPaletteModule = module {
    single<ThemePalette>(named(DISCO_ID)) {
        ThemePalette(
            id = DISCO_ID,
            titleRes = R.string.theme_palette_disco_title,
            taglineRes = R.string.theme_palette_disco_tagline,
            order = 1,
            colorScheme = DiscoColors,
            typography = DiscoTypography,
            systemDefault = SystemDefault.DARK,
        )
    }
}
