package ru.vkdev.dressdoll.theme.common

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isSpecified

/**
 * @param weight у текстового шрифта задаёт только body: title и label берут Medium из M3,
 *   поэтому семейство должно его содержать.
 */
@Immutable
class FontSpec(
    val family: FontFamily,
    val weight: FontWeight = FontWeight.Normal,
    val style: FontStyle = FontStyle.Normal,
    val letterSpacing: TextUnit = TextUnit.Unspecified,
)

fun buildTypography(display: FontSpec, body: FontSpec): Typography {
    val base = Typography()
    return Typography(
        displayLarge = base.displayLarge.with(display),
        displayMedium = base.displayMedium.with(display),
        displaySmall = base.displaySmall.with(display),
        headlineLarge = base.headlineLarge.with(display),
        headlineMedium = base.headlineMedium.with(display),
        headlineSmall = base.headlineSmall.with(display),
        titleLarge = base.titleLarge.with(body, keepWeight = true),
        titleMedium = base.titleMedium.with(body, keepWeight = true),
        titleSmall = base.titleSmall.with(body, keepWeight = true),
        bodyLarge = base.bodyLarge.with(body),
        bodyMedium = base.bodyMedium.with(body),
        bodySmall = base.bodySmall.with(body),
        labelLarge = base.labelLarge.with(body, keepWeight = true),
        labelMedium = base.labelMedium.with(body, keepWeight = true),
        labelSmall = base.labelSmall.with(body, keepWeight = true),
    )
}

private fun TextStyle.with(spec: FontSpec, keepWeight: Boolean = false): TextStyle = copy(
    fontFamily = spec.family,
    fontWeight = if (keepWeight) fontWeight else spec.weight,
    fontStyle = spec.style,
    letterSpacing = if (spec.letterSpacing.isSpecified) spec.letterSpacing else letterSpacing,
)
