package ru.vkdev.dressdoll.theme.api

import androidx.annotation.StringRes
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable

@Immutable
class ThemePalette(
    val id: String,
    @StringRes val titleRes: Int,
    val order: Int,
    val colorScheme: ColorScheme,
    val typography: Typography,
    val uppercaseHeadlines: Boolean = false,
)
