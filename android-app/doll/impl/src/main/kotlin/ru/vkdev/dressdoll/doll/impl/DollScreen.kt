package ru.vkdev.dressdoll.doll.impl

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.launch
import ru.vkdev.dressdoll.catalog.api.Catalog
import ru.vkdev.dressdoll.theme.api.AppIcons
import ru.vkdev.dressdoll.theme.api.HeaderBar
import ru.vkdev.dressdoll.theme.api.IconActionButton
import ru.vkdev.dressdoll.theme.api.Sizes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DollScreen(
    holder: DollStateHolder,
    catalog: Catalog,
    onOpenSettings: () -> Unit,
) {
    // Шторка не прячется целиком: «скрыта» — это PartiallyExpanded, виден только язычок.
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Expanded,
        skipHiddenState = true,
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
    val scope = rememberCoroutineScope()
    val worn by holder.worn.collectAsState()
    val wornIds = remember(worn) { worn?.mapTo(HashSet()) { it.id } }
    val scene by holder.scene.collectAsState()
    val isOpen = sheetState.currentValue == SheetValue.Expanded

    // Регистрируется раньше обработчика каталога, поэтому срабатывает, только когда каталог
    // «Назад» не забрал: в режиме категорий.
    BackHandler(enabled = isOpen) { scope.launch { sheetState.partialExpand() } }

    BoxWithConstraints {
        val screenHeight = maxHeight
        val sheetHeight = screenHeight / 2
        val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        // Язычок M3 вместе с полями высотой ровно в touchTarget.
        val peekHeight = Sizes.touchTarget + navigationBarHeight
        // Офсет шторки — от верха scaffold, то есть от верха экрана.
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
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                HeaderBar(
                    title = stringResource(R.string.doll_title),
                    onBack = null,
                    titleStyle = MaterialTheme.typography.headlineMedium,
                    minHeight = Sizes.topBarHeight,
                    modifier = Modifier.windowInsetsPadding(
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
                    onToggle = holder::toggle,
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
                DollCanvas(
                    scene = scene,
                    availableHeight = { with(density) { sheetTop() - contentTop.toPx() } },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

/** Насколько шторка раскрыта: 0 — скрыта, 1 — открыта. */
private fun openFraction(offset: Float, expandedOffset: Float, collapsedOffset: Float): Float =
    ((collapsedOffset - offset) / (collapsedOffset - expandedOffset)).coerceIn(0f, 1f)
