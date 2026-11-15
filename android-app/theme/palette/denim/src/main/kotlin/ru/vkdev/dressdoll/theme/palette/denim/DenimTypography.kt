package ru.vkdev.dressdoll.theme.palette.denim

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ru.vkdev.dressdoll.theme.common.CommonFonts
import ru.vkdev.dressdoll.theme.common.FontSpec
import ru.vkdev.dressdoll.theme.common.buildTypography
import ru.vkdev.dressdoll.theme.common.variableFontFamily

private val Oswald = variableFontFamily(R.font.oswald, FontWeight.SemiBold)

// Разрядка для узкого Oswald в верхнем регистре.
internal val DenimTypography: Typography = buildTypography(
    display = FontSpec(family = Oswald, weight = FontWeight.SemiBold, letterSpacing = 1.sp),
    body = FontSpec(family = CommonFonts.Rubik),
)
