package ru.vkdev.dressdoll.disco.impl

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

/** [x] — доля ширины области. */
internal enum class SpotSlot(val x: Float) {
    LEFT(1f / 6),
    CENTER(1f / 2),
    RIGHT(5f / 6),
}

internal enum class SpotColor { PRIMARY, SECONDARY, TERTIARY }

/** [x], [y] — доли прямоугольника фигурки. */
internal enum class SpotAim(val x: Float, val y: Float) {
    HEAD(0.5f, 0.08f),
    TORSO(0.5f, 0.35f),
    LEGS(0.5f, 0.7f),
}

/** [shift] — сдвиг вершины в долях ширины области, [phase] — радианы. */
internal data class Spot(
    val slot: SpotSlot,
    val shift: Float,
    val color: SpotColor,
    val aim: SpotAim,
    val phase: Float,
    val periodMillis: Int,
)

/** [angle] — радианы от вертикали вниз, положительный — вправо. */
internal data class Beam(val apex: Offset, val angle: Float, val length: Float)

internal fun nextSpots(random: Random): List<Spot> {
    val count = random.nextInt(1, SpotSlot.entries.size + 1)
    val slots = SpotSlot.entries.shuffled(random).take(count)
    val colors = SpotColor.entries.shuffled(random).take(count)
    return slots.zip(colors) { slot, color ->
        Spot(
            slot = slot,
            shift = (random.nextFloat() * 2 - 1) * MAX_SPOT_SHIFT,
            color = color,
            aim = SpotAim.entries.random(random),
            phase = random.nextFloat() * 2 * PI.toFloat(),
            periodMillis = random.nextInt(MIN_SWAY_PERIOD_MILLIS, MAX_SWAY_PERIOD_MILLIS + 1),
        )
    }
}

internal fun swayAngle(spot: Spot, timeMillis: Long): Float {
    // Остаток от периода, а не всё время: Float теряет точность на больших временах.
    val cycle = (timeMillis % spot.periodMillis).toFloat() / spot.periodMillis
    return MAX_SWAY * sin(2 * PI.toFloat() * cycle + spot.phase)
}

/** Луч доходит до нижнего края [area] целиком, когда [descent] = 1. */
internal fun beam(spot: Spot, area: Size, target: Offset, sway: Float, descent: Float): Beam {
    val apex = Offset(area.width * (spot.slot.x + spot.shift), -area.height * APEX_ABOVE)
    val angle = atan2(target.x - apex.x, target.y - apex.y) + sway
    val fullLength = (area.height - apex.y) / max(cos(angle), MIN_COS)
    return Beam(apex, angle, fullLength * decelerate(descent))
}

internal fun spotTarget(aim: SpotAim, figure: Rect): Offset =
    Offset(figure.left + aim.x * figure.width, figure.top + aim.y * figure.height)

private fun decelerate(t: Float): Float {
    val rest = 1 - t.coerceIn(0f, 1f)
    return 1 - rest * rest * rest
}

internal const val DISCO_FADE_MILLIS = 400
internal const val SPOT_CHANGE_MILLIS = 5000L

/** Доля ширины области. */
internal const val MAX_SPOT_SHIFT = 0.08f

internal const val MAX_SWAY = (PI / 18).toFloat()
/**
 * Около 30 обновлений качания в секунду: медленное качание реже кадров незаметно, а каждое обновление
 * перерисовывает три offscreen-слоя. Чуть меньше 1000/30, потому что время кадров округлено до мс.
 */
internal const val SWAY_STEP_MILLIS = 30L

internal const val MIN_SWAY_PERIOD_MILLIS = 4000
internal const val MAX_SWAY_PERIOD_MILLIS = 6000

/** Вершина выше области на эту долю её высоты: лампы не видно. */
internal const val APEX_ABOVE = 0.1f

/** Почти горизонтальный луч иначе получил бы бесконечную длину. */
private const val MIN_COS = 0.2f
