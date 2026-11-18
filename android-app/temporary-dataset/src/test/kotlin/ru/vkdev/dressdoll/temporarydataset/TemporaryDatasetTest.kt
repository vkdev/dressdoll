package ru.vkdev.dressdoll.temporarydataset

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
            decoder = AssetBitmapDecoder(catalogMinSide = 512, dollMipmap = true) { path -> File("src/main/assets", path).inputStream() },
            scope = CoroutineScope(SupervisorJob() + dispatcher),
            catalogCacheBytes = 64 * 1024 * 1024,
            dollCacheBytes = 64 * 1024 * 1024,
        ),
    )

    @Test
    fun `clothingCategories returns all categories in config order`() = runTest(dispatcher) {
        val categories = dataset.clothingCategories().first()
        val names = categories.map { it.name.value }

        assertEquals(DATASET_DIR.listFiles()!!.count { it.isDirectory }, names.size)
        assertEquals(listOf("бельевая-майка", "водолазка", "блузка"), names.take(3))
        assertEquals("галстук", names.last())
        assertNotEquals(names.sorted(), names)
        val sectionOrder = categoriesConfig.sections.map { it.id }
        val sectionIndexes = categories.map { sectionOrder.indexOf(it.section) }
        assertEquals(sectionIndexes.sorted(), sectionIndexes)
    }

    @Test
    fun `knee socks are drawn above underwear`() = runTest(dispatcher) {
        val categories = dataset.clothingCategories().first().associateBy { it.name.value }

        assertTrue(categories.getValue("гольфы").level.position > categories.getValue("бельевая-майка").level.position)
    }

    @Test
    fun `turtleneck is in undershirts group with limit 2`() = runTest(dispatcher) {
        val categories = dataset.clothingCategories().first().associateBy { it.name.value }

        val group = categories.getValue("водолазка").group
        assertEquals("undershirts", group.id.value)
        assertEquals(2, group.limit)
    }

    @Test
    fun `config has 10 sections in file order`() {
        val ids = categoriesConfig.sections.map { it.id.value }

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
        val category = CategoryName("ботинки")

        val items = dataset.clothingItems(category).first()

        assertEquals(listOf("ботинки 01", "ботинки 02", "ботинки 03"), items.map { it.name })
        assertTrue(items.all { it.id.category == category })
    }

    @Test
    fun `every category has items pointing back to it`() = runTest(dispatcher) {
        val categories = dataset.clothingCategories().first()

        val total = categories.sumOf { category ->
            val items = dataset.clothingItems(category.name).first()
            assertTrue("no items in ${category.name.value}", items.isNotEmpty())
            assertTrue(items.all { it.id.category == category.name })
            items.size
        }

        assertEquals(itemFolderCount(), total)
    }

    @Test
    fun `clothingItems for unknown category is empty`() = runTest(dispatcher) {
        assertEquals(emptyList<TDClothingItem>(), dataset.clothingItems(CategoryName("нет-такой")).first())
    }

    @Test
    fun `clothingItems id points to item folder`() = runTest(dispatcher) {
        val items = dataset.clothingItems(CategoryName("ботинки")).first()

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
        assertEquals(itemFolderCount(), items.size)
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
    fun `dollImages places item at offset from scale json`() = runTest(dispatcher) {
        val images = dataset.dollImages(TDItemId(CategoryName("блузка"), "01"))

        assertEquals(TDCanvasPoint(x = 215, y = 266), images.topLeft)
    }

    @Test
    fun `every item has a placement`() = runTest(dispatcher) {
        val placed = allItems().map { it.id }.toSet()

        assertEquals(placed, itemPlacements.keys)
    }

    @Test(expected = IllegalStateException::class)
    fun `dollImages for item without placement throws`() = runTest(dispatcher) {
        dataset.dollImages(TDItemId(CategoryName("нет-такой"), "01"))
    }

    @Test
    fun `figure is the whole doll canvas with figure bounds inside`() = runTest(dispatcher) {
        val figure = dataset.figure()

        assertEquals(768, figure.canvasWidth)
        assertEquals(1376, figure.canvasHeight)
        assertEquals(figure.canvasWidth, figure.image.width)
        assertEquals(figure.canvasHeight, figure.image.height)
        assertEquals(TDCanvasRect(left = 221, top = 66, width = 329, height = 1226), figure.bounds)
    }

    @Test
    fun `doll bitmaps have mipmaps, catalog bitmaps do not`() = runTest(dispatcher) {
        val id = TDItemId(CategoryName("блузка"), "01")

        assertTrue(dataset.figure().image.hasMipMap())
        assertTrue(dataset.dollImages(id).front.hasMipMap())
        assertTrue(dataset.dollImages(id).back!!.hasMipMap())
        assertFalse(dataset.catalogImage(id)!!.hasMipMap())
    }

    @Test
    fun `catalog bitmaps are downsampled`() = runTest(dispatcher) {
        val catalog = dataset.catalogImage(TDItemId(CategoryName("блузка"), "01"))!!

        assertEquals(384, catalog.width)
        assertEquals(512, catalog.height)
    }

    @Test
    fun `allClothingItems lists items of all categories in category order`() = runTest(dispatcher) {
        val all = dataset.allClothingItems().first()

        assertEquals(allItems(), all)
        assertEquals(itemFolderCount(), all.size)
    }

    @Test(expected = IllegalStateException::class)
    fun `figure without image throws`() = runTest(dispatcher) {
        val empty = TemporaryDataset(
            assets = assets,
            ioDispatcher = dispatcher,
            bitmaps = BitmapRepository(
                decoder = { null },
                scope = CoroutineScope(SupervisorJob() + dispatcher),
                catalogCacheBytes = 1024,
                dollCacheBytes = 1024,
            ),
        )

        empty.figure()
    }

    @Test
    fun `catalogImage for unknown item is null`() = runTest(dispatcher) {
        assertNull(dataset.catalogImage(TDItemId(CategoryName("нет-такой"), "01")))
    }

    private suspend fun allItems(): List<TDClothingItem> =
        dataset.clothingCategories().first().flatMap { dataset.clothingItems(it.name).first() }

    private fun itemFolderCount(): Int =
        DATASET_DIR.listFiles()!!.filter { it.isDirectory }.sumOf { category -> category.listFiles()!!.count { it.isDirectory } }

    private companion object {
        // Датасет меняется вместе с контентом, поэтому количества сверяются с папками, а не с числами.
        val DATASET_DIR = File("src/main/assets/$DATASET_ROOT")
    }
}
