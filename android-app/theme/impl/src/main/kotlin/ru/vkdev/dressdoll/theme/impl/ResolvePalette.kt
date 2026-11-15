package ru.vkdev.dressdoll.theme.impl

import ru.vkdev.dressdoll.theme.api.ThemeChoice
import ru.vkdev.dressdoll.theme.api.ThemeIds
import ru.vkdev.dressdoll.theme.api.ThemePalette

internal fun resolvePalette(
    choice: ThemeChoice,
    palettes: List<ThemePalette>,
    systemDark: Boolean,
): ThemePalette? {
    val id = when (choice) {
        ThemeChoice.System -> if (systemDark) ThemeIds.DISCO else ThemeIds.CINNAMON
        is ThemeChoice.Palette -> choice.id
    }
    return palettes.firstOrNull { it.id == id } ?: palettes.firstOrNull()
}
