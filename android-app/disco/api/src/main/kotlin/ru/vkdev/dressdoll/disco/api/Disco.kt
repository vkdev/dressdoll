package ru.vkdev.dressdoll.disco.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect

interface Disco {

    /** Режим переживает только пересоздание активити: уход с экрана и сворачивание его выключают. */
    @Composable
    fun rememberStage(): DiscoStage
}

/**
 * Модификаторы ставятся каждый на свой слой экрана: затемнение и лучи считаются в координатах
 * всего экрана, и слои рисуют их со сдвигом на своё положение.
 */
@Stable
interface DiscoStage {

    var isOn: Boolean

    /** 0…1, растёт при включении. Читать при отрисовке: значение меняется каждый кадр затухания. */
    val dim: Float

    /** Контейнер всего экрана под содержимым, после фона: тьма и лучи в воздухе. */
    val screen: Modifier

    /** Прозрачная шапка поверх лучей: тьма и свет ложатся только на её пиксели. */
    val header: Modifier

    /**
     * Слой фигурки масштаба 1: тьма и свет только на её пиксели. [figure] — прямоугольник, в который
     * целятся лучи, в координатах этого слоя; читается при отрисовке.
     */
    @Composable
    fun litFigure(figure: () -> Rect): Modifier
}
