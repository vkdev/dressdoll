package ru.vkdev.dressdoll.disco.impl

import androidx.activity.compose.LocalActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import ru.vkdev.dressdoll.disco.api.Disco
import ru.vkdev.dressdoll.disco.api.DiscoStage

internal class DiscoImpl(private val settings: DiscoSettingsRepository) : Disco {

    @Composable
    override fun rememberStage(): DiscoStage {
        val activity = LocalActivity.current
        // Пережить надо только пересоздание активити. Уход с экрана тоже сохраняет состояние записи
        // стека, а ON_STOP при этом не приходит: активити не останавливается.
        val on = rememberSaveable(
            stateSaver = Saver<Boolean, Boolean>(
                save = { it && activity?.isChangingConfigurations == true },
                restore = { it },
            ),
        ) { mutableStateOf(false) }
        val scheme = MaterialTheme.colorScheme
        val style by settings.beamStyle.collectAsStateWithLifecycle()
        val look = remember(scheme, style) { discoLook(scheme, style) }
        val stage = remember { DiscoStageImpl(on, DiscoState(initiallyOn = on.value), look) }
        SideEffect { stage.look = look }

        val state = stage.state
        LaunchedEffect(on.value) { state.play(on.value) }
        val scope = rememberCoroutineScope()
        LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
            if (activity?.isChangingConfigurations != true && on.value) {
                on.value = false
                scope.launch { state.cut() }
            }
        }
        val darkStatusBar by remember { derivedStateOf { state.dim.value > 0.5f } }
        if (darkStatusBar && activity != null) {
            val view = LocalView.current
            DisposableEffect(activity, view) {
                val controller = WindowCompat.getInsetsController(activity.window, view)
                val wasLight = controller.isAppearanceLightStatusBars
                controller.isAppearanceLightStatusBars = false
                onDispose { controller.isAppearanceLightStatusBars = wasLight }
            }
        }
        return stage
    }
}

// Модификаторы созданы один раз и читают оформление и прицел при отрисовке: рекомпозиция экрана их не пересоздаёт.
@Stable
private class DiscoStageImpl(
    private val on: MutableState<Boolean>,
    val state: DiscoState,
    initialLook: DiscoLook,
) : DiscoStage {

    private val layout = BeamLayout()

    var look: DiscoLook by mutableStateOf(initialLook)

    override var isOn: Boolean
        get() = on.value
        set(value) {
            on.value = value
        }

    override val dim: Float
        get() = state.dim.value

    override val screen: Modifier = Modifier.discoScreen(state, layout) { look }

    override val header: Modifier = Modifier.discoHeader(state, layout) { look }

    private val figureLayer: Modifier = Modifier.discoLitFigure(state, layout) { look }

    @Composable
    override fun litFigure(figure: () -> Rect): Modifier {
        val current by rememberUpdatedState(figure)
        SideEffect { layout.figure = { current() } }
        return figureLayer
    }
}
