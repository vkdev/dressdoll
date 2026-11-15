package ru.vkdev.dressdoll.theme.impl

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import ru.vkdev.dressdoll.theme.api.AppTheme

internal class AppThemeImpl : AppTheme {

    @Composable
    override fun Apply(content: @Composable () -> Unit) {
        MaterialTheme(
            colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
