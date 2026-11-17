package ru.vkdev.dressdoll.theme.api

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Отступы. Общие для всех палитр. */
object Spacing {
    val extraSmall: Dp = 4.dp
    val small: Dp = 8.dp
    val medium: Dp = 12.dp
    val large: Dp = 16.dp
    val extraLarge: Dp = 24.dp
    /** Запас под последним элементом прокручиваемого экрана. */
    val extraExtraLarge: Dp = 32.dp
    /** Запас вокруг содержимого, которое выступает за свои границы: одежда за фигуркой куклы. */
    val overhang: Dp = 30.dp
}

/** Размеры элементов. Общие для всех палитр. */
object Sizes {
    /** Минимальная область нажатия. */
    val touchTarget: Dp = 48.dp
    /** Минимальная высота шапки полноэкранного экрана. */
    val topBarHeight: Dp = 64.dp
    val icon: Dp = 24.dp
    val iconSmall: Dp = 18.dp
    /** Значок выбора в углу карточки. */
    val badge: Dp = 24.dp
    val tileHeight: Dp = 56.dp
}

/** Толщины линий. Общие для всех палитр. */
object Strokes {
    val thin: Dp = 1.dp
    /** Рамка выбранного элемента: одна во всём приложении. */
    val thick: Dp = 3.dp
}
