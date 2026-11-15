package ru.vkdev.dressdoll.theme.palette.avocado

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import ru.vkdev.dressdoll.theme.common.CommonFonts
import ru.vkdev.dressdoll.theme.common.FontSpec
import ru.vkdev.dressdoll.theme.common.buildTypography

private val Prata = FontFamily(Font(R.font.prata, FontWeight.Normal))

// Prata есть только Regular: другая насыщенность даст искусственный полужирный.
internal val AvocadoTypography: Typography = buildTypography(
    display = FontSpec(family = Prata, weight = FontWeight.Normal),
    body = FontSpec(family = CommonFonts.Rubik),
)
