package ru.vkdev.dressdoll.clothing.api

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow

/**
 * Правила ошибок:
 * - картинки, которой может не быть, нет — `null`;
 * - файл есть, но не читается — [java.io.IOException], что показать вместо картинки, решает потребитель;
 * - нарушен инвариант датасета, который проверяет сборка (нет позиции вещи, её `front.png` или фигурки), —
 *   [IllegalStateException]. Его не ловят: это ошибка сборки, а не данных.
 */
interface ClothingRepository {

    /** В порядке каталога. */
    fun clothingCategories(): Flow<List<ClothingCategory>>

    fun clothingItems(category: CategoryId): Flow<List<ClothingItem>>

    /** Все вещи датасета: по ним кукла проверяет сохранённый образ. */
    fun allClothingItems(): Flow<List<ClothingItem>>

    /** `null`, если у вещи нет картинки каталога. */
    suspend fun catalogImage(item: ClothingItemId): Bitmap?

    suspend fun dollImages(item: ClothingItemId): DollImages

    suspend fun figure(): DollFigure
}
