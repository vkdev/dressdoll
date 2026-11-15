package ru.vkdev.dressdoll.temporarydataset

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TemporaryDatasetTest {

    private val dispatcher = StandardTestDispatcher()
    private val dataset = TemporaryDataset(
        assets = ApplicationProvider.getApplicationContext<Context>().assets,
        ioDispatcher = dispatcher,
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
}
