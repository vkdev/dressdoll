package ru.vkdev.dressdoll.temporarydataset

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
import java.io.FileNotFoundException

@RunWith(RobolectricTestRunner::class)
class TemporaryDatasetTest {

    private val dispatcher = StandardTestDispatcher()
    private val assets = ApplicationProvider.getApplicationContext<Context>().assets

    // Robolectric's AssetManager.open() cannot find assets with non-ASCII (Cyrillic) paths,
    // although list() sees them, so images are read straight from the module's assets folder.
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
    fun `clothingCategories returns all category folders sorted`() = runTest(dispatcher) {
        val names = dataset.clothingCategories().first().map { it.name.value }

        assertEquals(34, names.size)
        assertEquals(names.sorted(), names)
        assertTrue("сапоги" in names)
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
            val catalog = dataset.catalogImage(item.id)
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

    @Test(expected = FileNotFoundException::class)
    fun `catalogImage for unknown item throws`() = runTest(dispatcher) {
        dataset.catalogImage(TDItemId(CategoryName("нет-такой"), "01"))
    }

    private suspend fun allItems(): List<TDClothingItem> =
        dataset.clothingCategories().first().flatMap { dataset.clothingItems(it.name).first() }
}
