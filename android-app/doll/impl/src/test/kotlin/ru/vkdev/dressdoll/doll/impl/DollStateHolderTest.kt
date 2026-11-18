package ru.vkdev.dressdoll.doll.impl

import android.graphics.Bitmap
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.vkdev.dressdoll.clothing.api.BodySlot
import ru.vkdev.dressdoll.clothing.api.CanvasPoint
import ru.vkdev.dressdoll.clothing.api.CanvasRect
import ru.vkdev.dressdoll.clothing.api.CategoryId
import ru.vkdev.dressdoll.clothing.api.ClothingCategory
import ru.vkdev.dressdoll.clothing.api.ClothingItem
import ru.vkdev.dressdoll.clothing.api.ClothingItemId
import ru.vkdev.dressdoll.clothing.api.ClothingRepository
import ru.vkdev.dressdoll.clothing.api.DollFigure
import ru.vkdev.dressdoll.clothing.api.DollImages
import ru.vkdev.dressdoll.outfit.api.OutfitRepository
import ru.vkdev.dressdoll.outfit.api.WornItem
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class DollStateHolderTest {

    private val outfit = FakeOutfit()
    private val repository = FakeRepository()

    @Test
    fun `сохранённый образ проверяется по датасету и сохраняется обратно`() = runTest(UnconfinedTestDispatcher()) {
        val gone = ClothingItemId(TSHIRT.category, "99")
        outfit.worn.value = listOf(TSHIRT, gone, TSHIRT).worn()

        val holder = holder()

        assertEquals(listOf(TSHIRT).worn(), holder.worn.value)
        assertEquals(listOf(listOf(TSHIRT).worn()), outfit.saves)
    }

    @Test
    fun `корректный образ не перезаписывается`() = runTest(UnconfinedTestDispatcher()) {
        outfit.worn.value = listOf(TSHIRT).worn()

        holder()

        assertEquals(emptyList<List<WornItem>>(), outfit.saves)
    }

    @Test
    fun `пока образ не прочитан, нажатие игнорируется`() = runTest(UnconfinedTestDispatcher()) {
        val holder = holder()

        holder.toggle(TSHIRT)

        assertNull(holder.worn.value)
        assertEquals(emptyList<List<WornItem>>(), outfit.saves)
    }

    @Test
    fun `убрать всё снимает все вещи`() = runTest(UnconfinedTestDispatcher()) {
        outfit.worn.value = listOf(PANTIES, SHIRT, BLAZER).worn()
        val holder = holder()

        holder.clearAll()

        assertEquals(emptyList<WornItem>(), holder.worn.value)
        assertEquals(emptyList<DollLayer>(), holder.scene.value!!.layers)
    }

    @Test
    fun `убрать всё до чтения образа не затирает его`() = runTest(UnconfinedTestDispatcher()) {
        val holder = holder()

        holder.clearAll()

        assertEquals(emptyList<List<WornItem>>(), outfit.saves)
    }

    @Test
    fun `нажатие надевает по правилам и сохраняет`() = runTest(UnconfinedTestDispatcher()) {
        outfit.worn.value = emptyList()
        val holder = holder()

        holder.toggle(TIGHTS)
        holder.toggle(STOCKINGS)

        assertEquals(listOf(STOCKINGS).worn(), holder.worn.value)
        assertEquals(listOf(STOCKINGS).worn(), outfit.saves.last())
    }

    @Test
    fun `второе нажатие до загрузки первого игнорируется`() = runTest(StandardTestDispatcher()) {
        outfit.worn.value = emptyList()
        val holder = holder()
        runCurrent()

        // Без runCurrent между нажатиями holder.worn и сцена ещё не видят первое.
        holder.toggle(PANTIES)
        holder.toggle(SHIRT)
        holder.clearAll()
        runCurrent()

        assertEquals(listOf(PANTIES).worn(), holder.worn.value)
        assertEquals(listOf(listOf(PANTIES).worn()), outfit.saves)
    }

    @Test
    fun `после загрузки сцены нажатия снова работают`() = runTest(UnconfinedTestDispatcher()) {
        outfit.worn.value = emptyList()
        val holder = holder()
        val gate = CompletableDeferred<Unit>()
        repository.gates[SHIRT] = gate

        holder.toggle(SHIRT)
        holder.toggle(PANTIES)
        gate.complete(Unit)
        runCurrent()
        holder.toggle(PANTIES)

        assertEquals(listOf(SHIRT, PANTIES).worn(), holder.worn.value)
    }

    @Test
    fun `в сцене задние плоскости в обратном порядке, передние — по уровням`() = runTest(UnconfinedTestDispatcher()) {
        outfit.worn.value = listOf(BLAZER, PANTIES, SHIRT).worn()

        val scene = checkNotNull(holder().scene.value)

        assertSame(repository.figureImage, scene.figure.image)
        assertEquals(
            listOf(
                BLAZER to Plane.BACK,
                SHIRT to Plane.BACK,
                PANTIES to Plane.BACK,
                PANTIES to Plane.FRONT,
                SHIRT to Plane.FRONT,
                BLAZER to Plane.FRONT,
            ),
            scene.layers.map { it.item to it.plane },
        )
        assertEquals(CanvasPoint(1, 2), scene.layers.first().topLeft)
    }

    @Test
    fun `сцена помнит свой образ, слой — слот категории`() = runTest(UnconfinedTestDispatcher()) {
        outfit.worn.value = listOf(SHIRT, TIGHTS).worn()

        val scene = checkNotNull(holder().scene.value)

        assertEquals(listOf(SHIRT, TIGHTS).worn(), scene.worn)
        assertEquals(
            mapOf(SHIRT to BodySlot.TORSO, TIGHTS to BodySlot.LEGS),
            scene.layers.associate { it.item to it.slot },
        )
    }

    @Test
    fun `пока картинки грузятся, образ сцены отстаёт от образа`() = runTest(UnconfinedTestDispatcher()) {
        outfit.worn.value = emptyList()
        val holder = holder()
        repository.gates[SHIRT] = CompletableDeferred()

        holder.toggle(SHIRT)

        assertEquals(listOf(SHIRT).worn(), holder.worn.value)
        assertEquals(emptyList<WornItem>(), holder.scene.value!!.worn)
    }

    @Test
    fun `вещь без задней плоскости даёт только передний слой`() = runTest(UnconfinedTestDispatcher()) {
        repository.withoutBack += SHIRT
        outfit.worn.value = listOf(SHIRT).worn()

        assertEquals(listOf(Plane.FRONT), holder().scene.value!!.layers.map { it.plane })
    }

    @Test
    fun `новая сцена появляется только после загрузки картинок`() = runTest(UnconfinedTestDispatcher()) {
        outfit.worn.value = listOf(PANTIES).worn()
        val holder = holder()
        val before = holder.scene.value
        val gate = CompletableDeferred<Unit>()
        repository.gates[SHIRT] = gate

        holder.toggle(SHIRT)

        assertSame(before, holder.scene.value)
        gate.complete(Unit)
        runCurrent()
        assertEquals(listOf(PANTIES, SHIRT), holder.scene.value!!.layers.filter { it.plane == Plane.FRONT }.map { it.item })
    }

    @Test
    fun `новый образ отменяет незаконченную загрузку`() = runTest(UnconfinedTestDispatcher()) {
        outfit.worn.value = emptyList()
        val holder = holder()
        repository.gates[SHIRT] = CompletableDeferred()

        holder.toggle(SHIRT)
        // Нажатия до загрузки игнорируются, поэтому новый образ приходит не из toggle.
        outfit.worn.value = emptyList()

        assertEquals(emptyList<DollLayer>(), holder.scene.value!!.layers)
    }

    @Test
    fun `вещь с нечитаемой картинкой пропускается на кукле, но остаётся в образе`() = runTest(UnconfinedTestDispatcher()) {
        repository.broken += SHIRT
        outfit.worn.value = listOf(PANTIES, SHIRT).worn()

        val holder = holder()

        assertEquals(listOf(PANTIES, SHIRT).worn(), holder.worn.value)
        assertEquals(setOf(PANTIES), holder.scene.value!!.layers.map { it.item }.toSet())
    }

    private fun TestScope.holder() = DollStateHolder(outfit, repository, backgroundScope)

    private fun List<ClothingItemId>.worn(): List<WornItem> = map(::WornItem)

    private class FakeOutfit : OutfitRepository {
        override val worn = MutableStateFlow<List<WornItem>?>(null)
        val saves = mutableListOf<List<WornItem>>()

        // Как OutfitRepositoryImpl: значение меняется сразу, до записи на диск.
        override fun save(worn: List<WornItem>) {
            saves += worn
            this.worn.value = worn
        }
    }

    private class FakeRepository : ClothingRepository {
        val figureImage: Bitmap = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)
        val gates = mutableMapOf<ClothingItemId, CompletableDeferred<Unit>>()
        val broken = mutableSetOf<ClothingItemId>()
        val withoutBack = mutableSetOf<ClothingItemId>()

        override fun clothingCategories(): Flow<List<ClothingCategory>> = flowOf(CATEGORIES.values.toList())

        override fun clothingItems(category: CategoryId): Flow<List<ClothingItem>> =
            flowOf(ITEMS.filter { it.category == category }.map { ClothingItem(it, it.category.value) })

        override fun allClothingItems(): Flow<List<ClothingItem>> =
            flowOf(ITEMS.map { ClothingItem(it, it.category.value) })

        override suspend fun catalogImage(item: ClothingItemId): Bitmap? = null

        override suspend fun dollImages(item: ClothingItemId): DollImages {
            gates[item]?.await()
            if (item in broken) throw IOException("broken")
            val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
            return DollImages(front = bitmap, back = bitmap.takeUnless { item in withoutBack }, topLeft = CanvasPoint(1, 2))
        }

        override suspend fun figure(): DollFigure =
            DollFigure(figureImage, canvasWidth = 4, canvasHeight = 4, bounds = CanvasRect(1, 1, 2, 2))
    }

    private companion object {
        val PANTIES = item("трусы")
        val TIGHTS = item("колготки")
        val STOCKINGS = item("чулки")
        val TSHIRT = item("футболка")
        val SHIRT = item("рубашка")
        val BLAZER = item("пиджак")

        val ITEMS = listOf(PANTIES, TIGHTS, STOCKINGS, TSHIRT, SHIRT, BLAZER)

        fun item(category: String) = ClothingItemId(CategoryId(category), "01")
    }
}
