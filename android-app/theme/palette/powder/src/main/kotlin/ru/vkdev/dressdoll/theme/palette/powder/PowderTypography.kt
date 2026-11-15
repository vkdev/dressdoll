package ru.vkdev.dressdoll.theme.palette.powder

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import ru.vkdev.dressdoll.theme.common.CommonFonts
import ru.vkdev.dressdoll.theme.common.FontSpec
import ru.vkdev.dressdoll.theme.common.buildTypography
import ru.vkdev.dressdoll.theme.common.variableFontFamily

private val CormorantItalic =
    variableFontFamily(R.font.cormorant_italic, FontWeight.Bold, style = FontStyle.Italic)

internal val PowderTypography: Typography = buildTypography(
    display = FontSpec(family = CormorantItalic, weight = FontWeight.Bold, style = FontStyle.Italic),
    body = FontSpec(family = CommonFonts.Manrope),
)
