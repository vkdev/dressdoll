package ru.vkdev.dressdoll.theme.impl

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.vkdev.dressdoll.theme.api.AppTheme
import ru.vkdev.dressdoll.theme.api.LocalThemePalette
import ru.vkdev.dressdoll.theme.api.ThemeSelector

private val DefaultColors = lightColorScheme()
private val DefaultTypography = Typography()

internal class AppThemeImpl(
    private val selector: ThemeSelector,
) : AppTheme {

    @Composable
    override fun Apply(content: @Composable () -> Unit) {
        val choice by selector.choice.collectAsStateWithLifecycle()
        val systemDark = isSystemInDarkTheme()
        // Структура дерева не зависит от choice: иначе при загрузке выбора контент
        // пересоздастся и потеряет восстановленное состояние.
        val palette = choice?.let { resolvePalette(it, selector.palettes, systemDark) }
        MaterialTheme(
            colorScheme = palette?.colorScheme ?: DefaultColors,
            typography = palette?.typography ?: DefaultTypography,
            shapes = AppShapes,
        ) {
            CompositionLocalProvider(LocalThemePalette provides palette, content = content)
        }
    }
}
