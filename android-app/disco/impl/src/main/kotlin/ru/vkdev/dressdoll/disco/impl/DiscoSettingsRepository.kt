package ru.vkdev.dressdoll.disco.impl

import kotlinx.coroutines.flow.StateFlow

internal enum class BeamStyle {
    /** Ровные цветные конусы без градиентов, как из бумаги. */
    PAPER,

    /** Свет в дымке: края и низ луча растворяются. */
    SOFT,
}

internal interface DiscoSettingsRepository {
    /** До чтения с диска — значение по умолчанию: режим при запуске выключен, и подмены не видно. */
    val beamStyle: StateFlow<BeamStyle>

    /** [beamStyle] меняется сразу, на диск пишется в фоне. */
    fun setBeamStyle(style: BeamStyle)
}
