package ru.vkdev.dressdoll.theme.impl

import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import ru.vkdev.dressdoll.theme.api.SystemDefault
import ru.vkdev.dressdoll.theme.api.ThemePalette

internal fun testPalette(id: String, order: Int = 0, systemDefault: SystemDefault? = null) = ThemePalette(
    id = id,
    titleRes = 0,
    taglineRes = 0,
    order = order,
    colorScheme = lightColorScheme(),
    typography = Typography(),
    systemDefault = systemDefault,
)
