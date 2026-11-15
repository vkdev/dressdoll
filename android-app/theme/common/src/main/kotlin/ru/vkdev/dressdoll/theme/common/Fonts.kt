package ru.vkdev.dressdoll.theme.common

import androidx.annotation.FontRes
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalTextApi::class)
fun variableFontFamily(
    @FontRes resId: Int,
    vararg weights: FontWeight,
    style: FontStyle = FontStyle.Normal,
): FontFamily = FontFamily(
    weights.map { weight ->
        Font(
            resId = resId,
            weight = weight,
            style = style,
            variationSettings = FontVariation.Settings(weight, style),
        )
    },
)

object CommonFonts {

    val Manrope: FontFamily = variableFontFamily(
        R.font.manrope,
        FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold,
    )

    val Rubik: FontFamily = variableFontFamily(
        R.font.rubik,
        FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold,
    )
}
