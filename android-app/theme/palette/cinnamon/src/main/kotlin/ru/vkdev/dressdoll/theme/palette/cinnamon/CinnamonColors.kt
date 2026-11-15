package ru.vkdev.dressdoll.theme.palette.cinnamon

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import ru.vkdev.dressdoll.theme.common.PaletteBaseColors
import ru.vkdev.dressdoll.theme.common.paletteColorScheme

internal val CinnamonColors: ColorScheme = paletteColorScheme(
    base = PaletteBaseColors(
        primary = Color(0xFFB5532E),
        onPrimary = Color(0xFFFFFFFF),
        secondary = Color(0xFFD9A23A),
        tertiary = Color(0xFF6F7140),
        background = Color(0xFFF3E6D0),
        surface = Color(0xFFFBF4E8),
        surfaceVariant = Color(0xFFE8D3B4),
        onSurface = Color(0xFF3A2414),
        outline = Color(0xFFA88C70),
    ),
    dark = false,
)
