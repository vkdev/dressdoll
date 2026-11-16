package ru.vkdev.dressdoll.repository.impl

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.vkdev.dressdoll.repository.api.CategoryId
import ru.vkdev.dressdoll.repository.api.ClothingCategory
import ru.vkdev.dressdoll.repository.api.ClothingItem
import ru.vkdev.dressdoll.repository.api.ClothingItemId
import ru.vkdev.dressdoll.temporarydataset.CategoryName
import ru.vkdev.dressdoll.temporarydataset.TDClothingCategory
import ru.vkdev.dressdoll.temporarydataset.TDClothingItem
import ru.vkdev.dressdoll.temporarydataset.TDGroup
import ru.vkdev.dressdoll.temporarydataset.TDGroupId
import ru.vkdev.dressdoll.temporarydataset.TDItemId
import ru.vkdev.dressdoll.temporarydataset.TDLevel
import ru.vkdev.dressdoll.temporarydataset.TDLevelId
import ru.vkdev.dressdoll.temporarydataset.TDSectionId
import ru.vkdev.dressdoll.temporarydataset.TDSlot

class TemporaryDatasetMappersTest {

    @Test
    fun `category maps name to id and title`() {
        assertEquals(
            ClothingCategory(id = CategoryId("сапоги"), title = "сапоги"),
            TDClothingCategory(
                name = CategoryName("сапоги"),
                slot = TDSlot.FEET,
                level = TDLevel(TDLevelId("shoes"), 4),
                group = TDGroup(TDGroupId("shoes"), 1),
                section = TDSectionId("shoes"),
            ).toClothingCategory(),
        )
    }

    @Test
    fun `item maps id and name`() {
        val item = TDClothingItem(id = TDItemId(CategoryName("сапоги"), "01"), name = "сапоги 01")

        assertEquals(
            ClothingItem(id = ClothingItemId(CategoryId("сапоги"), "01"), title = "сапоги 01"),
            item.toClothingItem(),
        )
    }

    @Test
    fun `item id round-trips to dataset id`() {
        val id = TDItemId(CategoryName("блузка"), "02")

        assertEquals(id, id.toClothingItemId().toTDItemId())
    }
}
