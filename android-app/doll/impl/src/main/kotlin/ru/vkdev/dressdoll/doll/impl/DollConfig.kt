package ru.vkdev.dressdoll.doll.impl

import kotlin.math.PI

internal object DollConfig {

    const val FLIGHT_MILLIS = 400

    /** Наибольшее отклонение дуги прилёта и глубина падения при снятии, в долях пути. */
    const val MAX_ARC = 0.2f

    const val TOP_SHIFT = 0.15f

    /** Почти горизонтальные края веера не берём. */
    const val TOSS_MIN_ANGLE = (PI / 9).toFloat()

    const val TOSS_MAX_ANGLE = (PI * 8 / 9).toFloat()

    /** Разброс угла в долях сектора одной вещи: соседние вещи не меняются местами. */
    const val TOSS_JITTER = 0.6f

    const val TOSS_MIN_APEX = 0.1f

    const val TOSS_MAX_APEX = 0.25f
}
