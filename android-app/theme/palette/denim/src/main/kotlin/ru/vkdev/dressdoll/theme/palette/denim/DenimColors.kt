package ru.vkdev.dressdoll.theme.palette.denim

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import ru.vkdev.dressdoll.theme.common.PaletteBaseColors
import ru.vkdev.dressdoll.theme.common.paletteColorScheme

internal val DenimColors: ColorScheme = paletteColorScheme(
    base = PaletteBaseColors(
        primary = Color(0xFF2F4A6D),
        onPrimary = Color(0xFFFFFFFF),
        secondary = Color(0xFFC98A2B),
        tertiary = Color(0xFFA4452C),
        background = Color(0xFFEFE4D2),
        surface = Color(0xFFFAF4EA),
        surfaceVariant = Color(0xFFD9CBB2),
        onSurface = Color(0xFF1F2733),
        outline = Color(0xFF9A9080),
    ),
    dark = false,
)
