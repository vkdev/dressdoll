package ru.vkdev.dressdoll.theme.api

sealed interface ThemeChoice {

    data object System : ThemeChoice

    data class Palette(val id: String) : ThemeChoice
}
