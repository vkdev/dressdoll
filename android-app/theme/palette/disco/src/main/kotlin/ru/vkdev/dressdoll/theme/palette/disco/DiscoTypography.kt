package ru.vkdev.dressdoll.theme.palette.disco

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import ru.vkdev.dressdoll.theme.common.FontSpec
import ru.vkdev.dressdoll.theme.common.buildTypography
import ru.vkdev.dressdoll.theme.common.variableFontFamily

private val PlayfairDisplayItalic =
    variableFontFamily(R.font.playfair_display_italic, FontWeight.Black, style = FontStyle.Italic)

private val MontserratAlternates = FontFamily(
    Font(R.font.montserrat_alternates_regular, FontWeight.Normal),
    Font(R.font.montserrat_alternates_medium, FontWeight.Medium),
)

internal val DiscoTypography: Typography = buildTypography(
    display = FontSpec(family = PlayfairDisplayItalic, weight = FontWeight.Black, style = FontStyle.Italic),
    body = FontSpec(family = MontserratAlternates),
)
