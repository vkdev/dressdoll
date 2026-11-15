package ru.vkdev.dressdoll.theme.api

import kotlinx.coroutines.flow.StateFlow

interface ThemeSelector {

    /** `null`, пока сохранённое значение ещё не прочитано. */
    val choice: StateFlow<ThemeChoice?>

    val palettes: List<ThemePalette>

    fun select(choice: ThemeChoice)
}
