package ru.vkdev.dressdoll.theme.palette.disco

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import ru.vkdev.dressdoll.theme.common.PaletteBaseColors
import ru.vkdev.dressdoll.theme.common.paletteColorScheme

internal val DiscoColors: ColorScheme = paletteColorScheme(
    base = PaletteBaseColors(
        primary = Color(0xFFE0559A),
        onPrimary = Color(0xFF24122E),
        secondary = Color(0xFFD4A84B),
        tertiary = Color(0xFFB9A6D6),
        background = Color(0xFF24122E),
        surface = Color(0xFF321C3F),
        surfaceVariant = Color(0xFF43284F),
        onSurface = Color(0xFFF6E9F0),
        outline = Color(0xFF7D6589),
    ),
    dark = true,
)
