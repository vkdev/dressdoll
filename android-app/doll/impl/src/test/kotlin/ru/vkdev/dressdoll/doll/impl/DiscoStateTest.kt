package ru.vkdev.dressdoll.doll.impl

import androidx.compose.runtime.BroadcastFrameClock
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class DiscoStateTest {

    @Test
    fun `на частых кадрах качание обновляется около 30 раз в секунду`() = runTest {
        for (hz in listOf(60, 90, 120)) {
            val times = swayUpdatesInSecondSecond(hz)

            assertTrue("$hz Гц: ${times.size} обновлений", times.size in 29..31)
            times.zipWithNext { a, b -> assertTrue("$hz Гц: шаг ${b - a} мс", b - a >= SWAY_STEP_MILLIS) }
        }
    }

    @Test
    fun `на редких кадрах качание обновляется каждый кадр`() = runTest {
        assertEquals(20, swayUpdatesInSecondSecond(hz = 20).size)
    }

    // Качание начинается после затемнения, а оно длится DISCO_FADE_MILLIS даже при уже полном затемнении,
    // поэтому считается вторая секунда кадров.
    private fun TestScope.swayUpdatesInSecondSecond(hz: Int): List<Long> {
        val clock = BroadcastFrameClock()
        val state = DiscoState(initiallyOn = true, Random(1))
        val job = launch(clock) { state.play(on = true) }
        runCurrent()
        val updates = mutableListOf<Long>()
        var last = state.timeMillis
        for (frame in 0 until 2 * hz) {
            clock.sendFrame(frame * 1_000_000_000L / hz)
            runCurrent()
            if (state.timeMillis != last) {
                last = state.timeMillis
                if (frame >= hz) updates += last
            }
        }
        job.cancel()
        return updates
    }
}
