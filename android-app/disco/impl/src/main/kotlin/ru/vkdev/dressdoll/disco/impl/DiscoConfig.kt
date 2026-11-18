package ru.vkdev.dressdoll.disco.impl

import kotlin.math.PI

internal object DiscoConfig {

    const val FADE_MILLIS = 400

    const val SPOT_CHANGE_MILLIS = 5000L

    /** Доля ширины области. */
    const val MAX_SPOT_SHIFT = 0.08f

    const val MAX_SWAY = (PI / 18).toFloat()

    /**
     * Около 30 обновлений качания в секунду: медленное качание реже кадров незаметно, а каждое обновление
     * перерисовывает три offscreen-слоя. Чуть меньше 1000/30, потому что время кадров округлено до мс.
     */
    const val SWAY_STEP_MILLIS = 30L

    const val MIN_SWAY_PERIOD_MILLIS = 4000

    const val MAX_SWAY_PERIOD_MILLIS = 6000

    /** Вершина выше области на эту долю её высоты: лампы не видно. */
    const val APEX_ABOVE = 0.1f

    /** Почти горизонтальный луч иначе получил бы бесконечную длину. */
    const val MIN_COS = 0.2f

    const val DIM_ALPHA = 0.6f

    /** Доля белого в цвете луча. */
    const val LIGHTEN = 0.45f

    const val BEAM_HALF_WIDTH = (PI / 180 * 7).toFloat()

    /**
     * Ровная середина луча — от [BEAM_CORE_START] до [BEAM_CORE_END] его ширины:
     * с треугольным профилем мягкий луч выходил тусклым.
     */
    const val BEAM_CORE_START = 0.3f

    const val BEAM_CORE_END = 0.7f

    /** Мягкий луч к концу гаснет: с этой доли длины непрозрачность падает от [BEAM_FADE_START_ALPHA] (0–255) до нуля. */
    const val BEAM_FADE_START = 0.6f

    const val BEAM_FADE_START_ALPHA = 0xE6

    /** Непрозрачность подкраски конуса цветом прожектора в воздухе. */
    const val PAPER_AIR_ALPHA = 0.3f

    const val SOFT_AIR_ALPHA = 0.35f

    /** То же на кукле, одежде и шапке. */
    const val PAPER_FIGURE_ALPHA = 0.25f

    const val SOFT_FIGURE_ALPHA = 0.3f
}
