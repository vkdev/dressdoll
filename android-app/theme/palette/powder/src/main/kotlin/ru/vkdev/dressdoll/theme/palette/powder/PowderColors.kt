package ru.vkdev.dressdoll.theme.palette.powder

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import ru.vkdev.dressdoll.theme.common.PaletteBaseColors
import ru.vkdev.dressdoll.theme.common.paletteColorScheme

internal val PowderColors: ColorScheme = paletteColorScheme(
    base = PaletteBaseColors(
        primary = Color(0xFF6E1F2E),
        onPrimary = Color(0xFFFFF6F2),
        secondary = Color(0xFFD9A5A0),
        tertiary = Color(0xFF8E9A7A),
        background = Color(0xFFF7E8E2),
        surface = Color(0xFFFFF6F2),
        surfaceVariant = Color(0xFFEBCFC8),
        onSurface = Color(0xFF2D2426),
        outline = Color(0xFFB0948F),
    ),
    dark = false,
)
