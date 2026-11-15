package ru.vkdev.dressdoll.theme.api

import androidx.compose.runtime.Composable

interface AppTheme {

    @Composable
    fun Apply(content: @Composable () -> Unit)
}
