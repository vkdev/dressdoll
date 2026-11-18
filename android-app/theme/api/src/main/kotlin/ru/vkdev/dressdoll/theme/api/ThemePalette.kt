package ru.vkdev.dressdoll.theme.api

import androidx.annotation.StringRes
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
class ThemePalette(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val taglineRes: Int,
    val order: Int,
    val colorScheme: ColorScheme,
    val typography: Typography,
    val uppercaseHeadlines: Boolean = false,
    val systemDefault: SystemDefault? = null,
)

/** `null`, если палитр нет и действует стандартная схема M3. */
val LocalThemePalette = staticCompositionLocalOf<ThemePalette?> { null }
