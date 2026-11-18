package ru.vkdev.dressdoll.doll.impl

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import ru.vkdev.dressdoll.catalog.api.Catalog
import ru.vkdev.dressdoll.clothing.api.ClothingItemId
import ru.vkdev.dressdoll.disco.api.Disco
import ru.vkdev.dressdoll.theme.ui.AppIcons
import ru.vkdev.dressdoll.theme.ui.HeaderBar
import ru.vkdev.dressdoll.theme.ui.IconActionButton
import ru.vkdev.dressdoll.theme.ui.IconToggleActionButton
import ru.vkdev.dressdoll.theme.ui.Sizes
import ru.vkdev.dressdoll.theme.ui.Spacing
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DollScreen(
    holder: DollStateHolder,
    catalog: Catalog,
    disco: Disco,
    onOpenSettings: () -> Unit,
) {
    // Шторка не прячется целиком: «скрыта» — это PartiallyExpanded, виден только язычок.
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Expanded,
        skipHiddenState = true,
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
    val scope = rememberCoroutineScope()
    val worn by holder.worn.collectAsStateWithLifecycle()
    val wornIds = remember(worn) { worn?.mapTo(HashSet()) { it.id } }
    val scene by holder.scene.collectAsStateWithLifecycle()
    val transition = remember { DollTransition() }
    // Пока грузятся картинки, нажатия отбрасывает holder, пока вещи летят — экран.
    val onToggle: (ClothingItemId) -> Unit = { if (!transition.isAnimating) holder.toggle(it) }
    val onClearAll = { if (!transition.isAnimating) holder.clearAll() }
    val isOpen = sheetState.currentValue == SheetValue.Expanded

    val discoStage = disco.rememberStage()
    val backdropAlpha = { discoStage.dim }
    val colorScheme = MaterialTheme.colorScheme

    // Регистрируется раньше обработчика каталога, поэтому срабатывает, только когда каталог
    // «Назад» не забрал: в режиме категорий.
    BackHandler(enabled = isOpen) { scope.launch { sheetState.partialExpand() } }

    // Фон рисует этот контейнер, а не Scaffold: иначе фон лёг бы поверх лучей, которые идут сквозь шапку.
    BoxWithConstraints(
        Modifier
            .background(colorScheme.background)
            .then(discoStage.screen),
    ) {
        val screenHeight = maxHeight
        val sheetHeight = screenHeight / 2
        val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        // Язычок M3 вместе с полями высотой ровно в touchTarget.
        val peekHeight = Sizes.touchTarget + navigationBarHeight
        val sheetTop: Density.() -> Float = {
            // На первом кадре слой собирается раньше, чем шторка выставила офсет.
            runCatching { sheetState.requireOffset() }.getOrElse {
                if (sheetState.currentValue == SheetValue.Expanded) {
                    (screenHeight - sheetHeight).toPx()
                } else {
                    (screenHeight - peekHeight).toPx()
                }
            }
        }
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = peekHeight,
            sheetDragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = Color.Transparent,
            topBar = {
                HeaderBar(
                    title = stringResource(R.string.doll_title),
                    onBack = null,
                    titleStyle = MaterialTheme.typography.headlineMedium,
                    minHeight = Sizes.topBarHeight,
                    modifier = Modifier
                        .then(discoStage.header)
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                        ),
                    actions = {
                        IconActionButton(
                            icon = AppIcons.Settings,
                            contentDescription = stringResource(R.string.doll_settings),
                            onClick = onOpenSettings,
                        )
                    },
                )
            },
            sheetContent = {
                catalog.Content(
                    worn = wornIds,
                    onToggle = onToggle,
                    isOpen = isOpen,
                    // Фиксированная высота делает раскрытую шторку вместе с язычком ровно половиной
                    // экрана: состояния выше половины нет.
                    modifier = Modifier
                        .height(sheetHeight - Sizes.touchTarget)
                        // Скрытая шторка выглядывает и под панелью навигации: без затухания там
                        // видна верхушка каталога, а должен быть только язычок.
                        .graphicsLayer {
                            alpha = openFraction(
                                offset = sheetTop(),
                                expandedOffset = (screenHeight - sheetHeight).toPx(),
                                collapsedOffset = (screenHeight - peekHeight).toPx(),
                            )
                        },
                )
            },
        ) {
            // Не innerPadding: в нём только нижний отступ под язычок, а верх контента — всегда 0.
            // Scaffold ставит контент под шапку и даёт ему высоту экрана без шапки.
            BoxWithConstraints(
                Modifier
                    .fillMaxSize()
                    // Шляпа или воротник за полями уходят под край шапки, а не рисуются поверх неё.
                    .clipToBounds()
                    // Вырез камеры сбоку в альбомной ориентации.
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
            ) {
                val contentTop = screenHeight - maxHeight
                val density = LocalDensity.current
                val availableHeight = { with(density) { sheetTop() - contentTop.toPx() } }
                val areaWidth = with(density) { maxWidth.toPx() }
                val overhang = with(density) { Spacing.overhang.toPx() }
                val figureBounds = scene?.figure?.bounds
                DollCanvas(
                    scene = scene,
                    transition = transition,
                    availableHeight = availableHeight,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(discoStage.litFigure { figureRect(figureBounds, areaWidth, availableHeight(), overhang) }),
                )
                IconToggleActionButton(
                    icon = AppIcons.DiscoBall,
                    contentDescription = stringResource(R.string.doll_disco),
                    checked = discoStage.isOn,
                    onCheckedChange = { discoStage.isOn = it },
                    backdropAlpha = backdropAlpha,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = Spacing.small)
                        .aboveSheet(availableHeight),
                )
                if (!worn.isNullOrEmpty()) {
                    IconActionButton(
                        icon = AppIcons.Hanger,
                        contentDescription = stringResource(R.string.doll_clear_all),
                        onClick = onClearAll,
                        backdropAlpha = backdropAlpha,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = Spacing.small)
                            .aboveSheet(availableHeight),
                    )
                }
            }
        }
    }
}

// Офсет читается при размещении: кнопка едет за шторкой без рекомпозиции.
private fun Modifier.aboveSheet(availableHeight: () -> Float): Modifier = offset {
    val bottom = availableHeight() - Spacing.small.toPx()
    IntOffset(0, (bottom - Sizes.touchTarget.toPx()).roundToInt())
}

private fun openFraction(offset: Float, expandedOffset: Float, collapsedOffset: Float): Float =
    ((collapsedOffset - offset) / (collapsedOffset - expandedOffset)).coerceIn(0f, 1f)
