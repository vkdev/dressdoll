package ru.vkdev.dressdoll.doll.impl

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.vkdev.dressdoll.repository.api.BodySlot
import kotlin.math.PI
import kotlin.math.abs
import kotlin.random.Random

class DollPathsTest {

    private val viewport = Rect(left = -50f, top = -20f, right = 400f, bottom = 900f)
    private val item = Rect(left = 100f, top = 300f, right = 250f, bottom = 500f)

    @Test
    fun `зоны прилёта по слотам`() {
        assertEquals(EntryZone.BOTTOM, BodySlot.FEET.entryZone())
        assertEquals(EntryZone.BOTTOM, BodySlot.LEGS.entryZone())
        assertEquals(EntryZone.MIDDLE, BodySlot.WAIST.entryZone())
        assertEquals(EntryZone.MIDDLE, BodySlot.TORSO.entryZone())
        assertEquals(EntryZone.MIDDLE, BodySlot.ARMS.entryZone())
        assertEquals(EntryZone.TOP, BodySlot.NECK.entryZone())
        assertEquals(EntryZone.TOP, BodySlot.HEAD.entryZone())
    }

    @Test
    fun `в начале прилёта вещь целиком за краем`() {
        EntryZone.entries.forEach { zone ->
            listOf(-MAX_ARC, 0f, MAX_ARC).forEach { bulge ->
                val start = item.translate(Entry(zone, bulge).offset(0f, item, viewport))
                assertTrue("$zone $bulge: $start", !start.overlaps(viewport))
            }
        }
    }

    @Test
    fun `снизу — вертикально, сверху — правее, середина — из левого верхнего угла`() {
        assertEquals(Offset(0f, 600f), entryStart(EntryZone.BOTTOM, item, viewport))
        assertEquals(Offset(450f * 0.15f, -520f), entryStart(EntryZone.TOP, item, viewport))
        // До верхнего края 520, до левого — 300: вещь уходит за левый.
        assertEquals(Offset(-300f, -300f), entryStart(EntryZone.MIDDLE, item, viewport))
    }

    @Test
    fun `прилёт кончается на месте вещи`() {
        EntryZone.entries.forEach { zone ->
            val end = Entry(zone, MAX_ARC).offset(1f, item, viewport)
            assertEquals(0f, end.x, EPS)
            assertEquals(0f, end.y, EPS)
        }
    }

    @Test
    fun `дуга отходит от прямой не больше чем на 20 процентов пути`() {
        val start = entryStart(EntryZone.MIDDLE, item, viewport)
        val length = start.getDistance()
        val deviations = (0..100).map { step ->
            val point = Entry(EntryZone.MIDDLE, MAX_ARC).offset(step / 100f, item, viewport)
            // Расстояние до прямой через ноль и start.
            abs(start.x * point.y - start.y * point.x) / length
        }
        assertEquals(MAX_ARC * length, deviations.max(), 1f)
    }

    @Test
    fun `прилёт с замедлением`() {
        val path = (0..10).map { Entry(EntryZone.BOTTOM, 0f).offset(it / 10f, item, viewport).y }
        val steps = path.zipWithNext { a, b -> a - b }
        assertTrue(steps.toString(), steps.zipWithNext().all { (a, b) -> a >= b })
    }

    @Test
    fun `снятие уходит вправо за край и вниз, без подъёма, с ускорением`() {
        val flight = Exit(drop = MAX_ARC)
        val path = (0..10).map { flight.offset(it / 10f, item, viewport) }

        assertEquals(Offset.Zero, path.first())
        assertTrue(item.translate(path.last()).left >= viewport.right)
        assertTrue(path.all { it.y >= 0f })
        assertEquals(300f * MAX_ARC, path.last().y, EPS)
        val steps = path.zipWithNext { a, b -> b.x - a.x }
        assertTrue(steps.toString(), steps.zipWithNext().all { (a, b) -> a <= b })
    }

    @Test
    fun `одна снятая вещь улетает вправо по половине параболы`() {
        repeat(100) { seed ->
            assertTrue(exitFlights(1, Random(seed)).single() is Exit)
        }
    }

    @Test
    fun `подброшенная вещь взлетает на заданную высоту и падает за нижний край`() {
        listOf(TOSS_MIN_ANGLE, PI.toFloat() / 2, TOSS_MAX_ANGLE).forEach { angle ->
            listOf(TOSS_MIN_APEX, TOSS_MAX_APEX).forEach { apex ->
                val flight = Toss(angle, apex)
                val path = (0..1000).map { flight.offset(it / 1000f, item, viewport) }

                assertEquals(0f, path.first().getDistance(), EPS)
                assertEquals("$angle $apex", -apex * viewport.height, path.minOf { it.y }, 1f)
                assertTrue("$angle $apex", item.translate(path.last()).top >= viewport.bottom)
            }
        }
    }

    @Test
    fun `подброс влево и вправо по углу`() {
        assertTrue(Toss(TOSS_MIN_ANGLE, TOSS_MIN_APEX).offset(1f, item, viewport).x > 0f)
        assertTrue(Toss(TOSS_MAX_ANGLE, TOSS_MIN_APEX).offset(1f, item, viewport).x < 0f)
        assertEquals(0f, Toss(PI.toFloat() / 2, TOSS_MIN_APEX).offset(1f, item, viewport).x, 1e-2f)
    }

    @Test
    fun `несколько снятых вещей подбрасываются веером в разные стороны`() {
        (2..7).forEach { count ->
            repeat(100) { seed ->
                val angles = exitFlights(count, Random(seed)).map { (it as Toss).angle }.sorted()
                val sector = (TOSS_MAX_ANGLE - TOSS_MIN_ANGLE) / count

                assertEquals(count, angles.size)
                assertTrue("$count: $angles", angles.all { it in TOSS_MIN_ANGLE..TOSS_MAX_ANGLE })
                assertTrue("$count: $angles", angles.first() < PI / 2 && angles.last() > PI / 2)
                // Разброс не сводит соседние углы ближе чем на полсектора.
                assertTrue("$count: $angles", angles.zipWithNext().all { (a, b) -> b - a > sector * 0.3f })
            }
        }
    }

    @Test
    fun `случайные параметры в пределах`() {
        val random = Random(42)
        repeat(1000) {
            assertTrue(abs(entryFlight(BodySlot.TORSO, random).bulge) <= MAX_ARC)
            assertTrue((exitFlights(1, random).single() as Exit).drop in 0f..MAX_ARC)
            assertTrue(exitFlights(3, random).all { (it as Toss).apex in TOSS_MIN_APEX..TOSS_MAX_APEX })
        }
    }

    private companion object {
        const val EPS = 1e-3f
    }
}
