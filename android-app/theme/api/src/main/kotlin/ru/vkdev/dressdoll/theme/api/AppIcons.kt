package ru.vkdev.dressdoll.theme.api

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Свои иконки вместо material-icons: их нужно несколько, и они в линейном стиле тем.
object AppIcons {

    val ArrowBack: ImageVector = lineIcon("ArrowBack", autoMirror = true) {
        moveTo(15f, 5f)
        lineTo(8f, 12f)
        lineTo(15f, 19f)
    }

    val ChevronRight: ImageVector = lineIcon("ChevronRight", autoMirror = true) {
        moveTo(9f, 6f)
        lineTo(15f, 12f)
        lineTo(9f, 18f)
    }

    val Check: ImageVector = lineIcon("Check") {
        moveTo(20f, 6f)
        lineTo(9f, 17f)
        lineTo(4f, 12f)
    }

    val Settings: ImageVector = lineIcon("Settings") {
        gearOutline(teeth = 8, outerRadius = 10f, innerRadius = 7.5f)
        moveTo(15f, 12f)
        arcTo(3f, 3f, 0f, isMoreThanHalf = true, isPositiveArc = true, x1 = 9f, y1 = 12f)
        arcTo(3f, 3f, 0f, isMoreThanHalf = true, isPositiveArc = true, x1 = 15f, y1 = 12f)
        close()
    }
}

/** Контур шестерёнки с центром в середине сетки 24; зуб сужается к вершине. */
private fun PathBuilder.gearOutline(teeth: Int, outerRadius: Float, innerRadius: Float) {
    val step = 2 * PI / teeth
    val baseHalf = step * 0.3
    val topHalf = step * 0.18
    fun x(radius: Float, angle: Double) = 12f + radius * cos(angle).toFloat()
    fun y(radius: Float, angle: Double) = 12f + radius * sin(angle).toFloat()

    // Первый зуб смотрит вверх.
    val start = -PI / 2
    moveTo(x(innerRadius, start - baseHalf), y(innerRadius, start - baseHalf))
    for (i in 0 until teeth) {
        val a = start + i * step
        lineTo(x(outerRadius, a - topHalf), y(outerRadius, a - topHalf))
        lineTo(x(outerRadius, a + topHalf), y(outerRadius, a + topHalf))
        lineTo(x(innerRadius, a + baseHalf), y(innerRadius, a + baseHalf))
        val next = a + step - baseHalf
        arcTo(
            innerRadius, innerRadius, 0f,
            isMoreThanHalf = false, isPositiveArc = true,
            x1 = x(innerRadius, next), y1 = y(innerRadius, next),
        )
    }
    close()
}

private fun lineIcon(
    name: String,
    autoMirror: Boolean = false,
    pathBuilder: PathBuilder.() -> Unit,
): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
    autoMirror = autoMirror,
).path(
    stroke = SolidColor(Color.Black),
    strokeLineWidth = 2f,
    strokeLineCap = StrokeCap.Round,
    strokeLineJoin = StrokeJoin.Round,
    pathBuilder = pathBuilder,
).build()
