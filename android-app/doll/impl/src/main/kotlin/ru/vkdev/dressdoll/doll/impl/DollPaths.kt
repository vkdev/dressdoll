package ru.vkdev.dressdoll.doll.impl

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import ru.vkdev.dressdoll.repository.api.BodySlot
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

// Пути полёта вещей. Все координаты — в пикселях холста куклы: [item] — прямоугольник картинки вещи
// на её месте, [viewport] — видимая область экрана в тех же координатах. Сдвиг — от места вещи.

internal enum class EntryZone {
    /** Прилетает снизу, из-под шторки. */
    BOTTOM,

    /** Прилетает из-за левого верхнего угла. */
    MIDDLE,

    /** Прилетает сверху со сдвигом вправо, чтобы не повторять путь средней зоны. */
    TOP,
}

internal fun BodySlot.entryZone(): EntryZone = when (this) {
    BodySlot.FEET, BodySlot.LEGS -> EntryZone.BOTTOM
    BodySlot.WAIST, BodySlot.TORSO, BodySlot.ARMS -> EntryZone.MIDDLE
    BodySlot.NECK, BodySlot.HEAD -> EntryZone.TOP
}

internal sealed interface Flight {
    /** [progress] — линейная доля времени анимации, от 0 до 1. */
    fun offset(progress: Float, item: Rect, viewport: Rect): Offset
}

/**
 * Прилёт на место по дуге с замедлением. [bulge] — отклонение середины дуги от прямой в долях длины
 * пути, знак задаёт сторону.
 */
internal data class Entry(val zone: EntryZone, val bulge: Float) : Flight {

    override fun offset(progress: Float, item: Rect, viewport: Rect): Offset {
        val start = entryStart(zone, item, viewport)
        // Квадратичная кривая Безье от start к нулю: середина отходит от прямой на половину сдвига
        // контрольной точки, поэтому контрольная точка сдвинута на удвоенное отклонение.
        val normal = Offset(-start.y, start.x)
        val control = start / 2f + normal * (2 * bulge)
        val rest = 1 - decelerate(progress)
        return start * (rest * rest) + control * (2 * rest * (1 - rest))
    }
}

/**
 * Снятие одной вещи: половина параболы вправо от вершины с ускорением. [drop] — глубина падения
 * в долях горизонтального пути.
 */
internal data class Exit(val drop: Float) : Flight {

    override fun offset(progress: Float, item: Rect, viewport: Rect): Offset {
        val distance = (viewport.right - item.left).coerceAtLeast(0f)
        val s = accelerate(progress)
        return Offset(distance * s, distance * drop * s * s)
    }
}

/**
 * Подбрасывание при «Снять всё»: вещь летит вверх под углом [angle] (радианы от горизонтали вправо,
 * против часовой стрелки) на высоту [apex] в долях высоты видимой области и падает за нижний край.
 * Время линейное, ускорение даёт само падение.
 */
internal data class Toss(val angle: Float, val apex: Float) : Flight {

    override fun offset(progress: Float, item: Rect, viewport: Rect): Offset {
        val t = progress.coerceIn(0f, 1f)
        val height = apex * viewport.height
        val fall = (viewport.bottom - item.top).coerceAtLeast(0f)
        // Скорость вверх vy и ускорение g подобраны так, чтобы вершина была на height,
        // а к концу вещь опустилась на fall: vy² = 2·g·height и g/2 − vy = fall.
        val vy = 2 * height + 2 * sqrt(height * height + height * fall)
        val g = 2 * (fall + vy)
        val vx = vy * cos(angle) / sin(angle)
        return Offset(vx * t, -vy * t + g * t * t / 2)
    }
}

internal fun entryFlight(slot: BodySlot, random: Random): Entry =
    Entry(slot.entryZone(), bulge = random.nextFloat() * 2 * MAX_ARC - MAX_ARC)

/**
 * Пути для [count] вещей, снятых разом. Одна вещь улетает вправо по [Exit]. Несколько, то есть
 * «Снять всё», подбрасываются веером: углы делят веер поровну с небольшим разбросом и раздаются
 * вещам случайно. Случайный угол у каждой мог бы отправить все вещи в одну сторону.
 */
internal fun exitFlights(count: Int, random: Random): List<Flight> {
    if (count == 1) return listOf(Exit(drop = random.nextFloat() * MAX_ARC))
    val sector = (TOSS_MAX_ANGLE - TOSS_MIN_ANGLE) / count
    return List(count) { index ->
        val jitter = (random.nextFloat() - 0.5f) * sector * TOSS_JITTER
        val angle = TOSS_MIN_ANGLE + sector * (index + 0.5f) + jitter
        Toss(angle, apex = TOSS_MIN_APEX + random.nextFloat() * (TOSS_MAX_APEX - TOSS_MIN_APEX))
    }.shuffled(random)
}

/** Сдвиг, при котором вещь целиком за краем [viewport]. */
internal fun entryStart(zone: EntryZone, item: Rect, viewport: Rect): Offset = when (zone) {
    EntryZone.BOTTOM -> Offset(0f, (viewport.bottom - item.top).coerceAtLeast(0f))
    EntryZone.TOP -> Offset(viewport.width * TOP_SHIFT, (viewport.top - item.bottom).coerceAtMost(0f))
    EntryZone.MIDDLE -> {
        // По диагонали до ближайшего края: левого или верхнего, за любым из них вещь уже не видна.
        val distance = min(item.right - viewport.left, item.bottom - viewport.top).coerceAtLeast(0f)
        Offset(-distance, -distance)
    }
}

internal fun decelerate(t: Float): Float {
    val rest = 1 - t.coerceIn(0f, 1f)
    return 1 - rest * rest * rest
}

internal fun accelerate(t: Float): Float {
    val clamped = t.coerceIn(0f, 1f)
    return clamped * clamped
}

internal const val FLIGHT_MILLIS = 400

/** Наибольшее отклонение дуги прилёта и глубина падения при снятии, в долях пути. */
internal const val MAX_ARC = 0.2f

/** Сдвиг вправо для верхней зоны, в долях ширины видимой области. */
private const val TOP_SHIFT = 0.15f

/** Веер подбрасывания при «Снять всё», радианы от горизонтали вправо. Почти горизонтальные края веера не берём. */
internal const val TOSS_MIN_ANGLE = (PI / 9).toFloat()
internal const val TOSS_MAX_ANGLE = (PI * 8 / 9).toFloat()

/** Разброс угла в долях сектора одной вещи: соседние вещи не меняются местами. */
private const val TOSS_JITTER = 0.6f

/** Высота подброса в долях высоты видимой области. */
internal const val TOSS_MIN_APEX = 0.1f
internal const val TOSS_MAX_APEX = 0.25f
