package ru.vkdev.dressdoll.theme.palette.avocado

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import ru.vkdev.dressdoll.theme.common.PaletteBaseColors
import ru.vkdev.dressdoll.theme.common.paletteColorScheme

internal val AvocadoColors: ColorScheme = paletteColorScheme(
    base = PaletteBaseColors(
        primary = Color(0xFF5F6E2F),
        onPrimary = Color(0xFFFFFFFF),
        secondary = Color(0xFFE0A526),
        tertiary = Color(0xFFC8602A),
        background = Color(0xFFF5EEDC),
        surface = Color(0xFFFFFAEE),
        surfaceVariant = Color(0xFFE4DDB8),
        onSurface = Color(0xFF3B2A1E),
        outline = Color(0xFFA89A74),
    ),
    dark = false,
)
