package ru.vkdev.dressdoll.disco.impl

import android.graphics.ComposeShader
import android.graphics.Matrix
import android.graphics.PorterDuff
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.SweepGradient
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.unit.toSize
import kotlin.math.PI
import kotlin.math.tan

@Immutable
internal class DiscoLook(
    private val primary: Color,
    private val secondary: Color,
    private val tertiary: Color,
    val style: BeamStyle,
) {
    fun color(role: SpotColor): Color = when (role) {
        SpotColor.PRIMARY -> primary
        SpotColor.SECONDARY -> secondary
        SpotColor.TERTIARY -> tertiary
    }

    /** Непрозрачность подкраски конуса цветом прожектора в воздухе. */
    val airAlpha: Float
        get() = when (style) {
            BeamStyle.PAPER -> 0.3f
            BeamStyle.SOFT -> 0.35f
        }

    /** То же на кукле, одежде и шапке. */
    val figureAlpha: Float
        get() = when (style) {
            BeamStyle.PAPER -> 0.25f
            BeamStyle.SOFT -> 0.3f
        }
}

// Акцентные цвета светлых палитр тёмные, и на затемнённом фоне сами по себе не светят.
internal fun discoLook(scheme: ColorScheme, style: BeamStyle) = DiscoLook(
    primary = lerp(scheme.primary, Color.White, LIGHTEN),
    secondary = lerp(scheme.secondary, Color.White, LIGHTEN),
    tertiary = lerp(scheme.tertiary, Color.White, LIGHTEN),
    style = style,
)

/**
 * Лучи считаются в координатах всего экрана, чтобы вершины были выше шапки и лучи шли сквозь неё.
 * Шапка и слой фигурки рисуют те же лучи со сдвигом на своё положение.
 */
internal class BeamLayout {
    var screen: LayoutCoordinates? = null
    var header: LayoutCoordinates? = null
    var figureLayer: LayoutCoordinates? = null

    /** Прямоугольник фигурки в координатах её слоя. */
    var figure: () -> Rect = { Rect.Zero }

    fun originOf(layer: LayoutCoordinates?): Offset {
        val screen = screen
        if (screen == null || layer == null || !screen.isAttached || !layer.isAttached) return Offset.Zero
        return screen.localPositionOf(layer, Offset.Zero)
    }

    fun screenSize(): Size = screen?.size?.toSize() ?: Size.Zero

    fun target(aim: SpotAim): Offset = originOf(figureLayer) + spotTarget(aim, figure())
}

/** На весь экран под содержимым: фон затемняется везде, кроме конусов. */
internal fun Modifier.discoScreen(state: DiscoState, layout: BeamLayout, look: () -> DiscoLook): Modifier =
    onPlaced { layout.screen = it }
        .drawWithCache {
            val painter = BeamPainter()
            onDrawBehind {
                val current = look()
                drawDisco(painter, state, layout, current, Offset.Zero, current.airAlpha, BlendMode.SrcOver)
            }
        }

/** Шапка прозрачная: тьма и свет ложатся только на заголовок и кнопки, фон под ней уже освещён. */
internal fun Modifier.discoHeader(state: DiscoState, layout: BeamLayout, look: () -> DiscoLook): Modifier =
    onPlaced { layout.header = it }
        .offscreenWhileDimmed(state)
        .drawWithCache {
            val painter = BeamPainter()
            onDrawWithContent {
                drawContent()
                val current = look()
                drawDisco(painter, state, layout, current, layout.originOf(layout.header), current.figureAlpha, BlendMode.SrcAtop)
            }
        }

/**
 * Тьма и свет ложатся только на пиксели фигурки (`SrcAtop`), поэтому слою нужен Offscreen. Отсюда
 * требование к слою масштаба 1: на слое с масштабом Offscreen растеризовал бы фигурку до увеличения.
 */
internal fun Modifier.discoLitFigure(state: DiscoState, layout: BeamLayout, look: () -> DiscoLook): Modifier =
    onPlaced { layout.figureLayer = it }
        .offscreenWhileDimmed(state)
        .drawWithCache {
            val painter = BeamPainter()
            onDrawWithContent {
                drawContent()
                val current = look()
                drawDisco(painter, state, layout, current, layout.originOf(layout.figureLayer), current.figureAlpha, BlendMode.SrcAtop)
            }
        }

private fun Modifier.offscreenWhileDimmed(state: DiscoState): Modifier = graphicsLayer {
    compositingStrategy = if (state.dim.value > 0f) CompositingStrategy.Offscreen else CompositingStrategy.Auto
}

/**
 * Тьма рисуется отдельным слоем, из которого конусы вырезаны (`DstOut`): внутри конуса яркость как без
 * затемнения. Поверх — подкраска конуса цветом прожектора. [origin] — положение слоя на экране.
 */
private fun DrawScope.drawDisco(
    painter: BeamPainter,
    state: DiscoState,
    layout: BeamLayout,
    look: DiscoLook,
    origin: Offset,
    tintAlpha: Float,
    blendMode: BlendMode,
) {
    val dim = state.dim.value
    if (dim <= 0f) return
    val beams = litBeams(state, layout, look)
    translate(-origin.x, -origin.y) {
        val bounds = Rect(origin, size)
        drawIntoCanvas { canvas ->
            canvas.saveLayer(bounds, painter.layerPaint(blendMode))
            drawRect(Color.Black, topLeft = origin, size = size, alpha = DIM_ALPHA * dim)
            beams.forEach { with(painter) { drawBeam(it.beam, Color.Black, it.alpha, BlendMode.DstOut, look.style) } }
            canvas.restore()
        }
        beams.forEach { with(painter) { drawBeam(it.beam, it.color, tintAlpha * dim * it.alpha, blendMode, look.style) } }
    }
}

/** [alpha] — гаснущий при смене набора прожектор, без затемнения. */
private class LitBeam(val beam: Beam, val color: Color, val alpha: Float)

private fun litBeams(state: DiscoState, layout: BeamLayout, look: DiscoLook): List<LitBeam> {
    val change = state.change.value
    val time = state.timeMillis
    val area = layout.screenSize()
    fun lit(spot: Spot, descent: Float, alpha: Float) = LitBeam(
        beam = beam(spot, area, layout.target(spot.aim), swayAngle(spot, time), descent),
        color = look.color(spot.color),
        alpha = alpha,
    )
    return state.leaving.map { lit(it, descent = 1f, alpha = 1f - change) } +
        state.current.map { lit(it, descent = change, alpha = 1f) }
}

/**
 * Луч задаётся преобразованием холста, а не своей геометрией: тогда путь и шейдеры единичного луча
 * (вершина в нуле, ось — y) переиспользуются, а не создаются для каждого луча на каждом кадре.
 */
private class BeamPainter {
    private val unitBeam = unitBeamPath()
    private val brushes = HashMap<Color, ShaderBrush>()
    private val layerPaint = Paint()

    fun layerPaint(blendMode: BlendMode): Paint = layerPaint.apply { this.blendMode = blendMode }

    fun DrawScope.drawBeam(beam: Beam, color: Color, alpha: Float, blendMode: BlendMode, style: BeamStyle) {
        if (beam.length < 1f || alpha <= 0f) return
        withTransform({
            translate(beam.apex.x, beam.apex.y)
            // Поворот холста по часовой стрелке уводит ось y влево, а положительный угол луча — вправо.
            rotate(-Math.toDegrees(beam.angle.toDouble()).toFloat(), pivot = Offset.Zero)
            scale(beam.length, beam.length, pivot = Offset.Zero)
        }) {
            when (style) {
                BeamStyle.SOFT -> {
                    val brush = brushes.getOrPut(color) { ShaderBrush(unitBeamShader(color)) }
                    drawPath(unitBeam, brush, alpha = alpha, blendMode = blendMode)
                }
                BeamStyle.PAPER -> drawPath(unitBeam, color, alpha = alpha, blendMode = blendMode)
            }
        }
    }
}

// Боковые стороны длиннее оси: дальний край треугольника касается окружности радиуса длины луча.
private fun unitBeamPath(): Path {
    val halfEnd = tan(BEAM_HALF_WIDTH)
    return Path().apply {
        moveTo(0f, 0f)
        lineTo(-halfEnd, 1f)
        lineTo(halfEnd, 1f)
        close()
    }
}

/** Поперёк луча — угловой градиент из вершины, вдоль — радиальный; они перемножаются через `DST_IN`. */
private fun unitBeamShader(color: Color): Shader {
    val solid = color.toArgb()
    val clear = color.copy(alpha = 0f).toArgb()
    val span = BEAM_HALF_WIDTH / PI.toFloat()
    // Ровная середина шириной 40% луча: с треугольным профилем луч выходил тусклым.
    val across = SweepGradient(
        0f, 0f,
        intArrayOf(clear, solid, solid, clear, clear),
        floatArrayOf(0f, span * 0.3f, span * 0.7f, span, 1f),
    )
    // SweepGradient отсчитывает угол от оси x, а ось луча — ось y, то есть 90°.
    val halfDegrees = Math.toDegrees(BEAM_HALF_WIDTH.toDouble()).toFloat()
    across.setLocalMatrix(Matrix().apply { setRotate(90f - halfDegrees) })
    val along = RadialGradient(
        0f, 0f, 1f,
        intArrayOf(0xFF000000.toInt(), 0xE6000000.toInt(), 0x00000000),
        floatArrayOf(0f, 0.6f, 1f),
        Shader.TileMode.CLAMP,
    )
    return ComposeShader(across, along, PorterDuff.Mode.DST_IN)
}

private const val DIM_ALPHA = 0.6f

/** Доля белого в цвете луча. */
private const val LIGHTEN = 0.45f

private const val BEAM_HALF_WIDTH = (PI / 180 * 7).toFloat()
