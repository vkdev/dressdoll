package ru.vkdev.dressdoll.disco.impl

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.MotionDurationScale
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Всё, кроме флага «включено», читается только при отрисовке.
 *
 * [initiallyOn] — режим восстановлен после пересоздания активити: затемнение сразу полное.
 */
@Stable
internal class DiscoState(initiallyOn: Boolean, private val random: Random = Random.Default) {

    val dim = Animatable(if (initiallyOn) 1f else 0f)

    /** 0 → 1: [current] спускаются, [leaving] гаснут. */
    val change = Animatable(1f)

    var current: List<Spot> by mutableStateOf(emptyList())
        private set

    var leaving: List<Spot> by mutableStateOf(emptyList())
        private set

    var timeMillis: Long by mutableLongStateOf(0L)
        private set

    suspend fun play(on: Boolean) {
        if (!on) {
            dim.animateTo(0f, tween(DiscoConfig.FADE_MILLIS))
            clear()
            return
        }
        coroutineScope {
            dim.animateTo(1f, tween(DiscoConfig.FADE_MILLIS))
            // Масштаб анимаций 0: Animatable и так завершаются сразу, а качание кадрами нужно выключить самим.
            if ((coroutineContext[MotionDurationScale]?.scaleFactor ?: 1f) > 0f) {
                launch { sway() }
            }
            while (true) {
                // После ON_STOP цикл не отменяется до возврата, а delay, в отличие от кадров, не встаёт на паузу.
                withFrameMillis {}
                launch { switchTo(nextSpots(random)) }
                delay(DiscoConfig.SPOT_CHANGE_MILLIS)
            }
        }
    }

    /** При сворачивании: кадров до возврата не будет, и плавное затухание доиграло бы уже на глазах. */
    suspend fun cut() {
        dim.snapTo(0f)
        clear()
    }

    private suspend fun switchTo(spots: List<Spot>) {
        leaving = current
        current = spots
        change.snapTo(0f)
        change.animateTo(1f, tween(DiscoConfig.FADE_MILLIS, easing = LinearEasing))
        leaving = emptyList()
    }

    // Кадр, в который время не записано, ничего не перерисовывает.
    private suspend fun sway() {
        val start = withFrameMillis { it } - timeMillis
        while (true) {
            withFrameMillis { now ->
                val time = now - start
                if (time - timeMillis >= DiscoConfig.SWAY_STEP_MILLIS) timeMillis = time
            }
        }
    }

    private suspend fun clear() {
        change.snapTo(1f)
        current = emptyList()
        leaving = emptyList()
    }
}
