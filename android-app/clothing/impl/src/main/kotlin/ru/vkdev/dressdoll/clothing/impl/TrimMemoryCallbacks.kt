package ru.vkdev.dressdoll.clothing.impl

import android.content.ComponentCallbacks2
import android.content.res.Configuration

internal class TrimMemoryCallbacks(
    private val trimMemory: (level: Int) -> Unit,
) : ComponentCallbacks2 {

    override fun onTrimMemory(level: Int) = trimMemory(level)

    @Suppress("DEPRECATION") // Самый сильный уровень: сбрасывает все кеши.
    override fun onLowMemory() = trimMemory(ComponentCallbacks2.TRIM_MEMORY_COMPLETE)

    override fun onConfigurationChanged(newConfig: Configuration) = Unit
}
