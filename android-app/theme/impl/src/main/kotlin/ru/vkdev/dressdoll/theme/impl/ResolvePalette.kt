package ru.vkdev.dressdoll.theme.impl

import ru.vkdev.dressdoll.theme.api.SystemDefault
import ru.vkdev.dressdoll.theme.api.ThemeChoice
import ru.vkdev.dressdoll.theme.api.ThemePalette

internal fun resolvePalette(
    choice: ThemeChoice,
    palettes: List<ThemePalette>,
    systemDark: Boolean,
): ThemePalette? {
    val chosen = when (choice) {
        ThemeChoice.System -> {
            val role = if (systemDark) SystemDefault.DARK else SystemDefault.LIGHT
            palettes.firstOrNull { it.systemDefault == role }
        }
        is ThemeChoice.Palette -> palettes.firstOrNull { it.id == choice.id }
    }
    return chosen ?: palettes.firstOrNull()
}
