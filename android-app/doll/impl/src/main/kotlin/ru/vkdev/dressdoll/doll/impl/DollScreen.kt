package ru.vkdev.dressdoll.doll.impl

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import ru.vkdev.dressdoll.catalog.api.Catalog
import ru.vkdev.dressdoll.repository.api.CategoryId
import ru.vkdev.dressdoll.repository.api.ClothingItemId
import ru.vkdev.dressdoll.theme.api.AppIcons
import ru.vkdev.dressdoll.theme.api.HeaderBar
import ru.vkdev.dressdoll.theme.api.IconActionButton
import ru.vkdev.dressdoll.theme.api.Sizes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DollScreen(
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
    var selected by rememberSaveable(saver = SelectedItemSaver) { mutableStateOf(null) }
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
                    selected = selected,
                    onSelect = { selected = it },
                    isOpen = isOpen,
                    // Фиксированная высота делает раскрытую шторку вместе с язычком ровно половиной
                    // экрана: состояния выше половины нет.
                    modifier = Modifier
                        .height(sheetHeight - Sizes.touchTarget)
                        // Скрытая шторка выглядывает и под панелью навигации: без затухания там
                        // видна верхушка каталога, а должен быть только язычок.
                        .graphicsLayer {
                            // На первом кадре слой собирается раньше, чем шторка выставила офсет.
                            val offset = runCatching { sheetState.requireOffset() }.getOrNull()
                            alpha = if (offset == null) {
                                if (sheetState.currentValue == SheetValue.Expanded) 1f else 0f
                            } else {
                                openFraction(
                                    offset = offset,
                                    expandedOffset = (screenHeight - sheetHeight).toPx(),
                                    collapsedOffset = (screenHeight - peekHeight).toPx(),
                                )
                            }
                        },
                )
            },
        ) {}
    }
}

/** Насколько шторка раскрыта: 0 — скрыта, 1 — открыта. */
private fun openFraction(offset: Float, expandedOffset: Float, collapsedOffset: Float): Float =
    ((collapsedOffset - offset) / (collapsedOffset - expandedOffset)).coerceIn(0f, 1f)

// ClothingItemId не Parcelable, поэтому категория и ключ лежат списком строк; пустой список — ничего не выбрано.
private val SelectedItemSaver = Saver<MutableState<ClothingItemId?>, List<String>>(
    save = { state -> state.value?.let { arrayListOf(it.category.value, it.key) } ?: arrayListOf() },
    restore = { saved ->
        mutableStateOf(saved.takeIf { it.size == 2 }?.let { ClothingItemId(CategoryId(it[0]), it[1]) })
    },
)
