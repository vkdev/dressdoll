package ru.vkdev.dressdoll.theme.palette.cinnamon

import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.vkdev.dressdoll.theme.api.SystemDefault
import ru.vkdev.dressdoll.theme.api.ThemePalette

const val CINNAMON_ID = "cinnamon"

val cinnamonPaletteModule = module {
    single<ThemePalette>(named(CINNAMON_ID)) {
        ThemePalette(
            id = CINNAMON_ID,
            titleRes = R.string.theme_palette_cinnamon_title,
            taglineRes = R.string.theme_palette_cinnamon_tagline,
            order = 0,
            colorScheme = CinnamonColors,
            typography = CinnamonTypography,
            systemDefault = SystemDefault.LIGHT,
        )
    }
}
