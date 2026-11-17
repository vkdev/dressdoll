package ru.vkdev.dressdoll.doll.impl

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import ru.vkdev.dressdoll.outfit.api.OutfitRepository
import ru.vkdev.dressdoll.outfit.api.WornItem
import ru.vkdev.dressdoll.repository.api.CanvasPoint
import ru.vkdev.dressdoll.repository.api.CategoryId
import ru.vkdev.dressdoll.repository.api.ClothingCategory
import ru.vkdev.dressdoll.repository.api.ClothingItemId
import ru.vkdev.dressdoll.repository.api.DollFigure
import ru.vkdev.dressdoll.repository.api.DollImages
import ru.vkdev.dressdoll.repository.api.DollRepository
import java.io.IOException

/** [layers] уже в порядке отрисовки снизу вверх; фигурка — между последним [Plane.BACK] и первым [Plane.FRONT]. */
internal data class DollScene(val figure: DollFigure, val layers: List<DollLayer>)

internal data class DollLayer(
    val item: ClothingItemId,
    val plane: Plane,
    val image: ImageBitmap,
    val topLeft: CanvasPoint,
)

internal enum class Plane { BACK, FRONT }

/**
 * Образ куклы и её сцена. Живёт всё время работы приложения и держит битмапы текущей сцены,
 * поэтому `onTrimMemory` их не освобождает, а только вытесняет из кеша.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class DollStateHolder(
    private val outfit: OutfitRepository,
    private val repository: DollRepository,
    scope: CoroutineScope,
) {

    private val dataset: StateFlow<Dataset?> = combine(
        repository.clothingCategories(),
        repository.allClothingItems(),
    ) { categories, items ->
        Dataset(categories.associateBy { it.id }, items.mapTo(mutableSetOf()) { it.id })
    }.stateIn(scope, SharingStarted.Eagerly, null)

    /** Надетые вещи в порядке надевания, проверенные по датасету; `null`, пока образ или датасет читаются. */
    val worn: StateFlow<List<WornItem>?> = combine(outfit.worn.filterNotNull(), dataset.filterNotNull()) { saved, dataset ->
        dataset.normalize(saved).also { if (it != saved) outfit.save(it) }
    }.stateIn(scope, SharingStarted.Eagerly, null)

    /**
     * Публикуется только после загрузки всех картинок образа: до этого на экране остаётся
     * предыдущая сцена. Новый образ отменяет незаконченную загрузку.
     */
    val scene: StateFlow<DollScene?> = worn.filterNotNull()
        .mapLatest { worn -> loadScene(drawOrder(worn, checkNotNull(dataset.value).categories)) }
        .stateIn(scope, SharingStarted.Eagerly, null)

    /** Пока образ не прочитан, нажатие игнорируется. */
    fun toggle(item: ClothingItemId) {
        val dataset = dataset.value ?: return
        if (worn.value == null) return
        // От образа в репозитории, а не от [worn]: тот обновляется асинхронно, и быстрое
        // второе нажатие потеряло бы первое.
        val saved = outfit.worn.value ?: return
        outfit.save(toggle(dataset.normalize(saved), item, dataset.categories))
    }

    private suspend fun loadScene(order: List<WornItem>): DollScene = coroutineScope {
        val figure = async { repository.figure() }
        val loaded = order.map { worn -> async { worn.id to loadImages(worn.id) } }.awaitAll()
        val placed = loaded.mapNotNull { (id, images) -> images?.let { id to it } }
        val backs = placed.asReversed().mapNotNull { (id, images) ->
            images.back?.let { DollLayer(id, Plane.BACK, it.asImageBitmap(), images.topLeft) }
        }
        val fronts = placed.map { (id, images) -> DollLayer(id, Plane.FRONT, images.front.asImageBitmap(), images.topLeft) }
        DollScene(figure.await(), backs + fronts)
    }

    // Нечитаемая картинка оставляет вещь в образе, но не на кукле: снять её можно обычным нажатием,
    // а временный сбой чтения не портит сохранённый образ.
    private suspend fun loadImages(item: ClothingItemId): DollImages? = try {
        repository.dollImages(item)
    } catch (_: IOException) {
        null
    }

    private class Dataset(val categories: Map<CategoryId, ClothingCategory>, val items: Set<ClothingItemId>) {
        fun normalize(saved: List<WornItem>) = normalize(saved, categories, items)
    }
}
