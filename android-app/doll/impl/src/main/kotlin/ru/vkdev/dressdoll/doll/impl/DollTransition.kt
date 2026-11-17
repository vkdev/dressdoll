package ru.vkdev.dressdoll.doll.impl

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ru.vkdev.dressdoll.repository.api.ClothingItemId
import kotlin.random.Random

/** Живёт, пока экран куклы в композиции: вернулись на экран — первая сцена показывается без анимации. */
@Stable
internal class DollTransition(private val random: Random = Random.Default) {

    var shown: DollScene? by mutableStateOf(null)
        private set

    var leaving: List<DollLayer> by mutableStateOf(emptyList())
        private set

    /** Прогресс общий: все анимации перехода идут одновременно. */
    var flights: Map<ClothingItemId, Flight> by mutableStateOf(emptyMap())
        private set

    val progress = Animatable(1f)

    val isAnimating: Boolean get() = flights.isNotEmpty()

    /** Отмена оставляет состояние для следующего вызова. */
    suspend fun show(scene: DollScene?) {
        val previous = shown
        if (previous == null || scene == null) {
            reset(scene)
            return
        }
        val before = previous.layers.mapTo(HashSet()) { it.item }
        val after = scene.layers.mapTo(HashSet()) { it.item }
        val entering = scene.layers.filter { it.item !in before }.associate { it.item to it.slot }
        val left = previous.layers.filter { it.item !in after }
        if (entering.isEmpty() && left.isEmpty()) {
            reset(scene)
            return
        }
        // Всё до первой приостановки попадает в один кадр: прилетающие вещи не мелькают на месте.
        progress.snapTo(0f)
        val leftItems = left.map { it.item }.distinct()
        flights = entering.mapValues { (_, slot) -> entryFlight(slot, random) } +
            leftItems.zip(exitFlights(leftItems.size, random)).toMap()
        leaving = left
        shown = scene
        progress.animateTo(1f, tween(FLIGHT_MILLIS, easing = LinearEasing))
        flights = emptyMap()
        leaving = emptyList()
    }

    private suspend fun reset(scene: DollScene?) {
        progress.snapTo(1f)
        flights = emptyMap()
        leaving = emptyList()
        shown = scene
    }
}
