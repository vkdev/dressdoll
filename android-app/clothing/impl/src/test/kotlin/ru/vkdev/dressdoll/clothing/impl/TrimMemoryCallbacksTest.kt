package ru.vkdev.dressdoll.clothing.impl

import android.content.ComponentCallbacks2
import org.junit.Assert.assertEquals
import org.junit.Test

class TrimMemoryCallbacksTest {

    private val levels = mutableListOf<Int>()
    private val callbacks = TrimMemoryCallbacks { levels += it }

    @Test
    fun `trim memory passes the level through`() {
        callbacks.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN)

        assertEquals(listOf(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN), levels)
    }

    @Suppress("DEPRECATION")
    @Test
    fun `low memory trims everything`() {
        callbacks.onLowMemory()

        assertEquals(listOf(ComponentCallbacks2.TRIM_MEMORY_COMPLETE), levels)
    }
}
