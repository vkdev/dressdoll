package ru.vkdev.dressdoll.temporarydataset

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.vkdev.dressdoll.temporarydataset.bitmap.AssetBitmapDecoder
import ru.vkdev.dressdoll.temporarydataset.bitmap.BitmapRepository
import java.io.File

@RunWith(RobolectricTestRunner::class)
class TemporaryDatasetTest {

    private val dispatcher = StandardTestDispatcher()
    private val assets = ApplicationProvider.getApplicationContext<Context>().assets

    // AssetManager.open() в Robolectric не находит ассеты с кириллицей в пути, хотя list() их видит,
    // поэтому картинки читаются прямо из папки ассетов модуля.
    private val dataset = TemporaryDataset(
        assets = assets,
        ioDispatcher = dispatcher,
        bitmaps = BitmapRepository(
            decoder = AssetBitmapDecoder { path -> File("src/main/assets", path).inputStream() },
            ioDispatcher = dispatcher,
            catalogCacheBytes = 64 * 1024 * 1024,
            dollCacheBytes = 64 * 1024 * 1024,
        ),
    )

    @Test
    fun `clothingCategories returns all categories in config order`() = runTest(dispatcher) {
        val categories = dataset.clothingCategories().first()
        val names = categories.map { it.name.value }

        assertEquals(34, names.size)
        assertEquals(listOf("трусы", "бюстгальтер", "бельевая-майка", "майка"), names.take(4))
        assertEquals("галстук", names.last())
        assertNotEquals(names.sorted(), names)
        val sectionOrder = dataset.catalogSections().first().map { it.id }
        val sectionIndexes = categories.map { sectionOrder.indexOf(it.section) }
        assertEquals(sectionIndexes.sorted(), sectionIndexes)
    }

    @Test
    fun `socks are drawn above tights`() = runTest(dispatcher) {
        val categories = dataset.clothingCategories().first().associateBy { it.name.value }

        assertTrue(categories.getValue("носки").level.position > categories.getValue("колготки").level.position)
    }

    @Test
    fun `tights and stockings share one group with limit 1`() = runTest(dispatcher) {
        val categories = dataset.clothingCategories().first().associateBy { it.name.value }

        val tights = categories.getValue("колготки").group
        assertEquals(tights, categories.getValue("чулки").group)
        assertEquals(1, tights.limit)
    }

    @Test
    fun `catalogSections returns 10 sections in config order`() = runTest(dispatcher) {
        val ids = dataset.catalogSections().first().map { it.id.value }

        assertEquals(
            listOf(
                "underwear", "tops", "sweaters", "jackets", "bottoms",
                "dresses", "outerwear", "hosiery", "shoes", "accessories",
            ),
            ids,
        )
    }

    @Test
    fun `clothingItems names items as category plus folder`() = runTest(dispatcher) {
        val category = CategoryName("сапоги")

        val items = dataset.clothingItems(category).first()

        assertEquals(listOf("сапоги 01", "сапоги 02", "сапоги 03"), items.map { it.name })
        assertTrue(items.all { it.parentCategory == category })
    }

    @Test
    fun `every category has items pointing back to it`() = runTest(dispatcher) {
        val categories = dataset.clothingCategories().first()

        val total = categories.sumOf { category ->
            val items = dataset.clothingItems(category.name).first()
            assertTrue("no items in ${category.name.value}", items.isNotEmpty())
            assertTrue(items.all { it.parentCategory == category.name })
            items.size
        }

        assertEquals(102, total)
    }

    @Test
    fun `clothingItems for unknown category is empty`() = runTest(dispatcher) {
        assertEquals(emptyList<TDClothingItem>(), dataset.clothingItems(CategoryName("нет-такой")).first())
    }

    @Test
    fun `clothingItems id points to item folder`() = runTest(dispatcher) {
        val items = dataset.clothingItems(CategoryName("сапоги")).first()

        assertEquals(listOf("01", "02", "03"), items.map { it.id.folder })
    }

    @Test
    fun `catalog and front images load for every item`() = runTest(dispatcher) {
        val items = allItems()

        items.forEach { item ->
            val catalog = checkNotNull(dataset.catalogImage(item.id)) { "no catalog ${item.name}" }
            val doll = dataset.dollImages(item.id)
            assertTrue("empty catalog ${item.name}", catalog.width > 0 && catalog.height > 0)
            assertTrue("empty front ${item.name}", doll.front.width > 0 && doll.front.height > 0)
        }
        assertEquals(102, items.size)
    }

    @Test
    fun `dollImages back is present for blouse and null for trousers`() = runTest(dispatcher) {
        assertNotNull(dataset.dollImages(TDItemId(CategoryName("блузка"), "01")).back)
        assertNull(dataset.dollImages(TDItemId(CategoryName("брюки"), "01")).back)
    }

    @Test
    fun `repeated request returns cached bitmap`() = runTest(dispatcher) {
        val id = TDItemId(CategoryName("блузка"), "01")

        assertSame(dataset.catalogImage(id), dataset.catalogImage(id))
        assertSame(dataset.dollImages(id).front, dataset.dollImages(id).front)
    }

    @Test
    fun `catalogImage for unknown item is null`() = runTest(dispatcher) {
        assertNull(dataset.catalogImage(TDItemId(CategoryName("нет-такой"), "01")))
    }

    private suspend fun allItems(): List<TDClothingItem> =
        dataset.clothingCategories().first().flatMap { dataset.clothingItems(it.name).first() }
}
