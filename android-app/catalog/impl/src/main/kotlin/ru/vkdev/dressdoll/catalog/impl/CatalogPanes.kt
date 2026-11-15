package ru.vkdev.dressdoll.catalog.impl

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import ru.vkdev.dressdoll.repository.api.CategoryId
import ru.vkdev.dressdoll.repository.api.ClothingCategory
import ru.vkdev.dressdoll.repository.api.ClothingItem
import ru.vkdev.dressdoll.repository.api.ClothingItemId
import ru.vkdev.dressdoll.repository.api.DollRepository
import ru.vkdev.dressdoll.theme.api.ChoiceTile
import ru.vkdev.dressdoll.theme.api.HeaderBar
import ru.vkdev.dressdoll.theme.api.NavigationTile
import ru.vkdev.dressdoll.theme.api.Spacing

/** [categories] — `null`, пока список не загружен. */
@Composable
internal fun CategoriesPane(
    categories: List<ClothingCategory>?,
    state: LazyGridState,
    bottomInset: Dp,
    onOpen: (ClothingCategory) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        HeaderBar(
            title = stringResource(R.string.catalog_title),
            onBack = null,
            supportingText = categories?.let {
                pluralStringResource(R.plurals.catalog_categories, it.size, it.size)
            },
        )
        // Сетка появляется только с данными: восстановленная прокрутка, применённая к пустому
        // списку, сбросилась бы в начало.
        if (categories == null) return@Column
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = state,
            contentPadding = gridPadding(bottomInset),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            items(categories, key = { it.id.value }) { category ->
                NavigationTile(
                    text = category.title.displayed(),
                    onClick = { onOpen(category) },
                )
            }
        }
    }
}

@Composable
internal fun ItemsPane(
    repository: DollRepository,
    category: ClothingCategory,
    loadedItems: MutableMap<CategoryId, List<ClothingItem>>,
    selected: ClothingItemId?,
    onSelect: (ClothingItemId) -> Unit,
    bottomInset: Dp,
    onBack: () -> Unit,
) {
    LaunchedEffect(repository, category.id) {
        repository.clothingItems(category.id).collect { loadedItems[category.id] = it }
    }
    val items = loadedItems[category.id]
    Column(Modifier.fillMaxSize()) {
        HeaderBar(
            title = category.title.displayed(),
            onBack = onBack,
            supportingText = items?.let {
                pluralStringResource(R.plurals.catalog_items, it.size, it.size)
            },
        )
        if (items == null) return@Column
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.selectableGroup(),
            contentPadding = gridPadding(bottomInset),
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium),
        ) {
            items(items, key = { it.id.key }) { item ->
                ChoiceTile(
                    selected = item.id == selected,
                    onClick = { onSelect(item.id) },
                    modifier = Modifier.aspectRatio(1f),
                ) {
                    ItemTileContent(repository = repository, item = item)
                }
            }
        }
    }
}

@Composable
private fun ItemTileContent(repository: DollRepository, item: ClothingItem) {
    val image by produceState<ImageBitmap?>(null, item.id) {
        value = repository.catalogImage(item.id)?.asImageBitmap()
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            // Подпись плитки уже озвучивает вещь, картинка декоративная.
            image?.let {
                Image(
                    bitmap = it,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Text(
            text = item.title.displayed(),
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// bottomInset — панель навигации: последний ряд не должен уходить под неё.
private fun gridPadding(bottomInset: Dp) = PaddingValues(
    start = Spacing.large,
    top = Spacing.large,
    end = Spacing.large,
    bottom = Spacing.large + bottomInset,
)

// Названия в репозитории в нижнем регистре, данные не меняем.
@Composable
private fun String.displayed(): String = capitalize(LocaleList.current)
