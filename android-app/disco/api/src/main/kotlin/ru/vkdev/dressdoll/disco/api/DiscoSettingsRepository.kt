package ru.vkdev.dressdoll.disco.api

import kotlinx.coroutines.flow.StateFlow

enum class BeamStyle {
    /** Ровные цветные конусы без градиентов, как из бумаги. */
    PAPER,

    /** Свет в дымке: края и низ луча растворяются. */
    SOFT,
}

interface DiscoSettingsRepository {
    /** До чтения с диска — значение по умолчанию: режим при запуске выключен, и подмены не видно. */
    val beamStyle: StateFlow<BeamStyle>

    /** [beamStyle] меняется сразу, на диск пишется в фоне. */
    fun setBeamStyle(style: BeamStyle)
}
