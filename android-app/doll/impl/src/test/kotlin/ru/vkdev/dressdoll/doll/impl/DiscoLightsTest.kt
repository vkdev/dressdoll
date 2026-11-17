package ru.vkdev.dressdoll.doll.impl

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.vkdev.dressdoll.repository.api.CanvasRect
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class DiscoLightsTest {

    private val area = Size(1000f, 2000f)

    @Test
    fun `от одного до трёх прожекторов, слоты и цвета без повторов`() {
        val counts = HashSet<Int>()
        repeat(500) { seed ->
            val spots = nextSpots(Random(seed))
            counts += spots.size
            assertTrue(spots.size in 1..3)
            assertEquals(spots.size, spots.map { it.slot }.distinct().size)
            assertEquals(spots.size, spots.map { it.color }.distinct().size)
        }
        assertEquals(setOf(1, 2, 3), counts)
    }

    @Test
    fun `одно зерно — один набор`() {
        repeat(20) { seed ->
            assertEquals(nextSpots(Random(seed)), nextSpots(Random(seed)))
        }
    }

    @Test
    fun `случайные параметры в пределах`() {
        val random = Random(7)
        repeat(1000) {
            nextSpots(random).forEach {
                assertTrue(abs(it.shift) <= MAX_SPOT_SHIFT)
                assertTrue(it.periodMillis in MIN_SWAY_PERIOD_MILLIS..MAX_SWAY_PERIOD_MILLIS)
            }
        }
    }

    @Test
    fun `качание не выходит за 10 градусов и повторяется с периодом`() {
        val spot = nextSpots(Random(3)).first()
        val angles = (0L..12_000L step 10).map { swayAngle(spot, it) }
        assertTrue(angles.all { abs(it) <= MAX_SWAY + 1e-6f })
        assertEquals(MAX_SWAY, angles.max(), 1e-3f)
        assertEquals(swayAngle(spot, 1234), swayAngle(spot, 1234L + spot.periodMillis), 1e-5f)
    }

    @Test
    fun `луч без качания проходит через прицел и доходит до низа`() {
        val target = Offset(420f, 900f)
        SpotSlot.entries.forEach { slot ->
            val spot = Spot(slot, shift = 0.05f, SpotColor.PRIMARY, SpotAim.TORSO, phase = 0f, periodMillis = 5000)
            val beam = beam(spot, area, target, sway = 0f, descent = 1f)

            assertTrue(beam.apex.y < 0f)
            val toTarget = target - beam.apex
            val direction = Offset(sin(beam.angle), cos(beam.angle))
            assertEquals(0f, toTarget.x * direction.y - toTarget.y * direction.x, 1e-2f)
            assertEquals(area.height, beam.apex.y + beam.length * direction.y, 1e-1f)
        }
    }

    @Test
    fun `луч спускается от нуля с замедлением`() {
        val spot = Spot(SpotSlot.LEFT, 0f, SpotColor.SECONDARY, SpotAim.HEAD, 0f, 5000)
        val lengths = (0..10).map { beam(spot, area, Offset(500f, 500f), 0f, it / 10f).length }
        assertEquals(0f, lengths.first(), 1e-3f)
        val steps = lengths.zipWithNext { a, b -> b - a }
        assertTrue(steps.toString(), steps.all { it >= 0f } && steps.zipWithNext().all { (a, b) -> a >= b })
    }

    @Test
    fun `прицел в долях фигурки, пока кукла не помещается — середина области`() {
        val bounds = CanvasRect(left = 100, top = 0, width = 200, height = 1000)
        val head = spotTarget(SpotAim.HEAD, bounds, areaWidth = 400f, availableHeight = 1000f, margin = 0f)
        val legs = spotTarget(SpotAim.LEGS, bounds, areaWidth = 400f, availableHeight = 1000f, margin = 0f)
        assertEquals(Offset(200f, 80f), head)
        assertEquals(Offset(200f, 700f), legs)
        assertEquals(Offset(200f, 5f), spotTarget(SpotAim.TORSO, bounds, 400f, 10f, margin = 20f))
        assertEquals(Offset(200f, 500f), spotTarget(SpotAim.TORSO, null, 400f, 1000f, 0f))
    }
}
