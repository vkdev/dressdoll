package ru.vkdev.dressdoll.catalog.impl

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.onConsumedWindowInsetsChanged
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.vkdev.dressdoll.catalog.api.Catalog
import ru.vkdev.dressdoll.repository.api.CategoryId
import ru.vkdev.dressdoll.repository.api.ClothingItem
import ru.vkdev.dressdoll.repository.api.ClothingItemId
import ru.vkdev.dressdoll.repository.api.DollRepository

internal class CatalogImpl(
    private val repository: DollRepository,
) : Catalog {

    @Composable
    override fun Content(
        worn: Set<ClothingItemId>?,
        onToggle: (ClothingItemId) -> Unit,
        isOpen: Boolean,
        modifier: Modifier,
    ) {
        var openCategory by rememberSaveable(saver = OpenCategorySaver) { mutableStateOf(null) }
        val categories by remember(repository) { repository.clothingCategories() }
            .collectAsStateWithLifecycle(initialValue = null)
        // Над AnimatedContent: иначе после возврата из категории список прокручен в начало.
        val categoriesState = rememberLazyGridState()
        // Повторно открытая категория сразу показывает вещи, пока поток присылает свежий список.
        val loadedItems = remember(repository) { mutableStateMapOf<CategoryId, List<ClothingItem>>() }

        // Инсеты, которые уже применил кто-то выше, второй раз не добавляем.
        var consumedInsets by remember { mutableStateOf(WindowInsets(0, 0, 0, 0)) }
        val bottomInset = WindowInsets.navigationBars
            .exclude(consumedInsets)
            .only(WindowInsetsSides.Bottom)
            .asPaddingValues()
            .calculateBottomPadding()

        BackHandler(enabled = isOpen && openCategory != null) { openCategory = null }

        AnimatedContent(
            targetState = openCategory,
            transitionSpec = {
                val direction = if (targetState != null) SlideDirection.Start else SlideDirection.End
                slideIntoContainer(direction) togetherWith slideOutOfContainer(direction)
            },
            modifier = modifier
                // safeDrawing, а не navigationBars: в альбомной ориентации сбоку бывает вырез камеры.
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .onConsumedWindowInsetsChanged { consumedInsets = it },
            label = "catalogMode",
        ) { category ->
            if (category == null) {
                CategoriesPane(
                    categories = categories,
                    state = categoriesState,
                    bottomInset = bottomInset,
                    onOpen = { openCategory = OpenCategory(it.id, it.title) },
                )
            } else {
                ItemsPane(
                    repository = repository,
                    category = category,
                    loadedItems = loadedItems,
                    worn = worn,
                    onToggle = onToggle,
                    bottomInset = bottomInset,
                    onBack = { openCategory = null },
                )
            }
        }
    }
}

/** Только то, что нужно экрану вещей: после смерти процесса шапка не ждёт список категорий. */
internal data class OpenCategory(val id: CategoryId, val title: String)

// OpenCategory не Parcelable, поэтому id и название лежат списком строк; пустой список — категория не открыта.
private val OpenCategorySaver = Saver<MutableState<OpenCategory?>, List<String>>(
    save = { state -> state.value?.let { arrayListOf(it.id.value, it.title) } ?: arrayListOf() },
    restore = { saved ->
        mutableStateOf(saved.takeIf { it.size == 2 }?.let { OpenCategory(CategoryId(it[0]), it[1]) })
    },
)
