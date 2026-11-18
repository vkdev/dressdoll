package ru.vkdev.dressdoll.catalog.impl

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import ru.vkdev.dressdoll.clothing.api.CategoryId
import ru.vkdev.dressdoll.clothing.api.ClothingCategory
import ru.vkdev.dressdoll.clothing.api.ClothingItem
import ru.vkdev.dressdoll.clothing.api.ClothingItemId
import ru.vkdev.dressdoll.clothing.api.ClothingRepository
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

/**
 * Потоки списков живут, пока на них подписан экран, и держат последнее значение:
 * повторно открытая категория сразу показывает прошлый список, пока поток присылает свежий.
 */
internal class CatalogStateHolder(
    private val repository: ClothingRepository,
    private val scope: CoroutineScope,
) {

    /** `null` до загрузки. */
    val categories: StateFlow<List<ClothingCategory>?> =
        repository.clothingCategories().stateIn(scope, SharingStarted.WhileSubscribed(), null)

    private val items = ConcurrentHashMap<CategoryId, StateFlow<List<ClothingItem>?>>()

    /** `null` до первой загрузки категории. */
    fun items(category: CategoryId): StateFlow<List<ClothingItem>?> = items.computeIfAbsent(category) {
        repository.clothingItems(category).stateIn(scope, SharingStarted.WhileSubscribed(), null)
    }

    /** Битая или нечитаемая картинка оставляет плитку без изображения, а не роняет приложение. */
    suspend fun image(item: ClothingItemId): ImageBitmap? = try {
        repository.catalogImage(item)?.asImageBitmap()
    } catch (_: IOException) {
        null
    }
}
