package ru.vkdev.dressdoll.catalog.impl

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.vkdev.dressdoll.clothing.api.BodySlot
import ru.vkdev.dressdoll.clothing.api.CategoryId
import ru.vkdev.dressdoll.clothing.api.ClothingCategory
import ru.vkdev.dressdoll.clothing.api.ClothingItem
import ru.vkdev.dressdoll.clothing.api.ClothingItemId
import ru.vkdev.dressdoll.clothing.api.ClothingRepository
import ru.vkdev.dressdoll.clothing.api.DollFigure
import ru.vkdev.dressdoll.clothing.api.DollImages
import ru.vkdev.dressdoll.clothing.api.DrawLevel
import ru.vkdev.dressdoll.clothing.api.DrawLevelId
import ru.vkdev.dressdoll.clothing.api.ReplacementGroup
import ru.vkdev.dressdoll.clothing.api.ReplacementGroupId
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class CatalogStateHolderTest {

    private val repository = FakeRepository()

    @Test
    fun `категории null до загрузки, затем список`() = runTest(UnconfinedTestDispatcher()) {
        val holder = holder()

        assertNull(holder.categories.value)
        backgroundScope.launch { holder.categories.collect {} }
        repository.categories.value = listOf(BOOTS)

        assertEquals(listOf(BOOTS), holder.categories.value)
    }

    @Test
    fun `повторно открытая категория сразу показывает прошлый список`() = runTest(UnconfinedTestDispatcher()) {
        val holder = holder()
        val first = launch { holder.items(BOOTS.id).collect {} }
        assertEquals(listOf(BOOTS_01), holder.items(BOOTS.id).value)
        first.cancel()
        repository.itemLoads = 0

        repository.itemsGate = MutableStateFlow(false)
        val second = launch { holder.items(BOOTS.id).collect {} }

        assertEquals(listOf(BOOTS_01), holder.items(BOOTS.id).value)
        assertEquals(1, repository.itemLoads)
        second.cancel()
    }

    @Test
    fun `одна категория — один поток`() = runTest(UnconfinedTestDispatcher()) {
        val holder = holder()

        assertSame(holder.items(BOOTS.id), holder.items(BOOTS.id))
    }

    @Test
    fun `нечитаемая картинка даёт null`() = runTest(UnconfinedTestDispatcher()) {
        repository.broken += BOOTS_01.id

        assertNull(holder().image(BOOTS_01.id))
    }

    @Test
    fun `картинка без файла даёт null`() = runTest(UnconfinedTestDispatcher()) {
        repository.missing += BOOTS_01.id

        assertNull(holder().image(BOOTS_01.id))
    }

    @Test
    fun `картинка переводится в ImageBitmap`() = runTest(UnconfinedTestDispatcher()) {
        val image = holder().image(BOOTS_01.id)

        assertNotNull(image)
        assertEquals(2, image!!.width)
    }

    private fun TestScope.holder() = CatalogStateHolder(repository, backgroundScope)

    private class FakeRepository : ClothingRepository {
        val categories = MutableStateFlow<List<ClothingCategory>?>(null)
        val broken = mutableSetOf<ClothingItemId>()
        val missing = mutableSetOf<ClothingItemId>()
        var itemsGate = MutableStateFlow(true)
        var itemLoads = 0

        override fun clothingCategories(): Flow<List<ClothingCategory>> = categories.filterNotNull()

        override fun clothingItems(category: CategoryId): Flow<List<ClothingItem>> = flow {
            itemLoads++
            itemsGate.first { it }
            emit(listOf(BOOTS_01).filter { it.id.category == category })
        }

        override fun allClothingItems(): Flow<List<ClothingItem>> = flowOf(listOf(BOOTS_01))

        override suspend fun catalogImage(item: ClothingItemId): Bitmap? {
            if (item in broken) throw IOException("broken")
            if (item in missing) return null
            return Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
        }

        override suspend fun dollImages(item: ClothingItemId): DollImages = error("not used")

        override suspend fun figure(): DollFigure = error("not used")
    }

    private companion object {
        val BOOTS = ClothingCategory(
            id = CategoryId("сапоги"),
            title = "сапоги",
            slot = BodySlot.FEET,
            level = DrawLevel(DrawLevelId("shoes"), 1),
            group = ReplacementGroup(ReplacementGroupId("shoes"), 1),
        )
        val BOOTS_01 = ClothingItem(ClothingItemId(BOOTS.id, "01"), "сапоги 01")
    }
}
