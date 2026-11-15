package ru.vkdev.dressdoll.theme.palette.cinnamon

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import ru.vkdev.dressdoll.theme.common.CommonFonts
import ru.vkdev.dressdoll.theme.common.FontSpec
import ru.vkdev.dressdoll.theme.common.buildTypography

private val YesevaOne = FontFamily(Font(R.font.yeseva_one, FontWeight.Normal))

// Yeseva One есть только Regular: другая насыщенность даст искусственный полужирный.
internal val CinnamonTypography: Typography = buildTypography(
    display = FontSpec(family = YesevaOne, weight = FontWeight.Normal),
    body = FontSpec(family = CommonFonts.Manrope),
)
