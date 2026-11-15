package ru.vkdev.dressdoll.navigation.impl

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Те же проверки на обычном списке и на настоящем [NavBackStack] (snapshot-список). */
abstract class NavigatorImplTest {

    protected abstract fun createBackStack(start: NavKey): MutableList<NavKey>

    private data object A : NavKey
    private data object B : NavKey
    private data object C : NavKey

    private val backStack by lazy { createBackStack(A) }
    private val navigator by lazy { NavigatorImpl(backStack) }

    @Test
    fun `single screen cannot go back`() {
        assertFalse(navigator.canGoBack)
    }

    @Test
    fun `navigate pushes route on top`() {
        navigator.navigate(B)
        navigator.navigate(C)

        assertEquals(listOf(A, B, C), backStack.toList())
        assertTrue(navigator.canGoBack)
    }

    @Test
    fun `back pops top route`() {
        navigator.navigate(B)

        assertTrue(navigator.back())
        assertEquals(listOf(A), backStack.toList())
        assertFalse(navigator.canGoBack)
    }

    @Test
    fun `back on last screen keeps it and returns false`() {
        assertFalse(navigator.back())
        assertEquals(listOf(A), backStack.toList())
    }

    @Test
    fun `replaceAll leaves only new route`() {
        navigator.navigate(B)

        navigator.replaceAll(C)

        assertEquals(listOf(C), backStack.toList())
        assertFalse(navigator.canGoBack)
    }

    @Test
    fun `replaceAll with current single route keeps it`() {
        navigator.replaceAll(A)

        assertEquals(listOf(A), backStack.toList())
    }
}

class ListNavigatorImplTest : NavigatorImplTest() {
    override fun createBackStack(start: NavKey): MutableList<NavKey> = mutableListOf(start)
}

class NavBackStackNavigatorImplTest : NavigatorImplTest() {
    override fun createBackStack(start: NavKey): MutableList<NavKey> = NavBackStack(start)
}
