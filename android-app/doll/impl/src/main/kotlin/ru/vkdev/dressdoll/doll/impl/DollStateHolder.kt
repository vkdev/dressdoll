package ru.vkdev.dressdoll.doll.impl

import android.graphics.Bitmap
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
import ru.vkdev.dressdoll.repository.api.BodySlot
import ru.vkdev.dressdoll.repository.api.CanvasPoint
import ru.vkdev.dressdoll.repository.api.CategoryId
import ru.vkdev.dressdoll.repository.api.ClothingCategory
import ru.vkdev.dressdoll.repository.api.ClothingItemId
import ru.vkdev.dressdoll.repository.api.DollFigure
import ru.vkdev.dressdoll.repository.api.DollImages
import ru.vkdev.dressdoll.repository.api.DollRepository
import java.io.IOException

/**
 * [layers] уже в порядке отрисовки снизу вверх; фигурка — между последним [Plane.BACK] и первым [Plane.FRONT].
 * [worn] — образ, по которому собрана сцена: пока он не совпал с [DollStateHolder.worn], картинки ещё грузятся.
 */
internal data class DollScene(val figure: DollFigure, val layers: List<DollLayer>, val worn: List<WornItem>)

internal data class DollLayer(
    val item: ClothingItemId,
    val plane: Plane,
    val image: ImageBitmap,
    val topLeft: CanvasPoint,
    val slot: BodySlot,
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
        .mapLatest { worn -> loadScene(worn, checkNotNull(dataset.value).categories) }
        .stateIn(scope, SharingStarted.Eagerly, null)

    /** Пока образ не прочитан или картинки прошлого нажатия не загружены, нажатие игнорируется. */
    fun toggle(item: ClothingItemId) {
        val dataset = dataset.value ?: return
        val current = settledOutfit(dataset) ?: return
        outfit.save(toggle(current, item, dataset.categories))
    }

    /**
     * Снимает всё. Пока образ не прочитан, игнорируется: иначе пустой образ затёр бы сохранённый.
     * Пока картинки прошлого нажатия не загружены — тоже, как и [toggle].
     */
    fun clearAll() {
        val dataset = dataset.value ?: return
        settledOutfit(dataset) ?: return
        outfit.save(emptyList())
    }

    /**
     * Образ, если сцена уже собрана по нему, иначе `null`. От образа в репозитории, а не от [worn]:
     * тот обновляется асинхронно, и быстрое второе нажатие проскочило бы до загрузки первого.
     */
    private fun settledOutfit(dataset: Dataset): List<WornItem>? {
        if (worn.value == null) return null
        val current = dataset.normalize(outfit.worn.value ?: return null)
        return current.takeIf { it == scene.value?.worn }
    }

    private suspend fun loadScene(
        worn: List<WornItem>,
        categories: Map<CategoryId, ClothingCategory>,
    ): DollScene = coroutineScope {
        val figure = async { repository.figure() }
        val loaded = drawOrder(worn, categories).map { item -> async { item.id to loadImages(item.id) } }.awaitAll()
        val placed = loaded.mapNotNull { (id, images) -> images?.let { id to it } }
        fun layer(id: ClothingItemId, plane: Plane, image: Bitmap, images: DollImages) =
            DollLayer(id, plane, image.asImageBitmap(), images.topLeft, categories.getValue(id.category).slot)
        val backs = placed.asReversed().mapNotNull { (id, images) -> images.back?.let { layer(id, Plane.BACK, it, images) } }
        val fronts = placed.map { (id, images) -> layer(id, Plane.FRONT, images.front, images) }
        DollScene(figure.await(), backs + fronts, worn)
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
