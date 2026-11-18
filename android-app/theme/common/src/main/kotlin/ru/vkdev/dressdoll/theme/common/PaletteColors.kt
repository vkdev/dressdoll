package ru.vkdev.dressdoll.theme.common

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.roundToInt

@Immutable
class PaletteBaseColors(
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val tertiary: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onSurface: Color,
    val outline: Color,
)

fun paletteColorScheme(base: PaletteBaseColors, dark: Boolean): ColorScheme = with(base) {
    // В светлой теме «светлый» on-цвет — белый, «тёмный» — цвет текста.
    // В тёмной — наоборот: светлый текст и тёмный фон.
    val lightOn = if (dark) onSurface else Color.White
    val darkOn = if (dark) background else onSurface
    val containerBase = if (dark) background else surface
    val containerMix = if (dark) PaletteColorsConfig.DARK_CONTAINER_MIX else PaletteColorsConfig.LIGHT_CONTAINER_MIX

    fun container(color: Color) = mix(color, containerBase, containerMix)
    fun onContainer(color: Color) = deepen(color, toward = onSurface, PaletteColorsConfig.CONTAINER_CONTRAST, container(color))

    val surfaces = if (dark) {
        SurfaceSteps(
            dim = background,
            bright = mix(surfaceVariant, Color.White, 0.08f),
            lowest = mix(background, Color.Black, 0.3f),
            low = mix(background, surface, 0.5f),
            container = surface,
            high = mix(surface, surfaceVariant, 0.5f),
            highest = surfaceVariant,
        )
    } else {
        SurfaceSteps(
            dim = mix(background, surfaceVariant, 0.35f),
            bright = surface,
            lowest = mix(surface, Color.White, 0.6f),
            low = surface,
            container = mix(surface, surfaceVariant, 0.25f),
            high = mix(surface, surfaceVariant, 0.5f),
            highest = mix(surface, surfaceVariant, 0.75f),
        )
    }
    val inversePrimary = if (dark) mix(primary, Color.Black, 0.35f) else mix(primary, Color.White, 0.45f)
    val onSurfaceVariant = deepen(mix(onSurface, surfaceVariant, 0.35f), onSurface, PaletteColorsConfig.VARIANT_CONTRAST, surfaceVariant)

    val scheme = if (dark) darkColorScheme() else lightColorScheme()
    scheme.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = container(primary),
        onPrimaryContainer = onContainer(primary),
        inversePrimary = inversePrimary,
        secondary = secondary,
        onSecondary = higherContrast(secondary, lightOn, darkOn),
        secondaryContainer = container(secondary),
        onSecondaryContainer = onContainer(secondary),
        tertiary = tertiary,
        onTertiary = higherContrast(tertiary, lightOn, darkOn),
        tertiaryContainer = container(tertiary),
        onTertiaryContainer = onContainer(tertiary),
        background = background,
        onBackground = onSurface,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        surfaceTint = primary,
        inverseSurface = onSurface,
        inverseOnSurface = surface,
        outline = outline,
        outlineVariant = mix(outline, surface, 0.5f),
        surfaceBright = surfaces.bright,
        surfaceDim = surfaces.dim,
        surfaceContainerLowest = surfaces.lowest,
        surfaceContainerLow = surfaces.low,
        surfaceContainer = surfaces.container,
        surfaceContainerHigh = surfaces.high,
        surfaceContainerHighest = surfaces.highest,
    )
}

private class SurfaceSteps(
    val dim: Color,
    val bright: Color,
    val lowest: Color,
    val low: Color,
    val container: Color,
    val high: Color,
    val highest: Color,
)

/** По WCAG 2: от 1 до 21. */
fun contrast(a: Color, b: Color): Float {
    val la = a.luminance()
    val lb = b.luminance()
    return (maxOf(la, lb) + 0.05f) / (minOf(la, lb) + 0.05f)
}

// Линейно по каналам sRGB, а не lerp из Compose (Oklab): на этой формуле проверялись цвета холста.
internal fun mix(a: Color, b: Color, fraction: Float): Color {
    fun channel(x: Float, y: Float) = ((x + (y - x) * fraction) * 255).roundToInt() / 255f
    return Color(channel(a.red, b.red), channel(a.green, b.green), channel(a.blue, b.blue))
}

private fun higherContrast(color: Color, light: Color, dark: Color): Color =
    if (contrast(color, light) >= contrast(color, dark)) light else dark

private fun deepen(color: Color, toward: Color, minContrast: Float, background: Color): Color {
    for (step in 0..PaletteColorsConfig.DEEPEN_STEPS) {
        val candidate = mix(color, toward, step.toFloat() / PaletteColorsConfig.DEEPEN_STEPS)
        if (contrast(candidate, background) >= minContrast) return candidate
    }
    return toward
}
