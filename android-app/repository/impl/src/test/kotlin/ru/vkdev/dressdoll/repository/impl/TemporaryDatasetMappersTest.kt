package ru.vkdev.dressdoll.repository.impl

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.vkdev.dressdoll.repository.api.BodySlot
import ru.vkdev.dressdoll.repository.api.CanvasPoint
import ru.vkdev.dressdoll.repository.api.CanvasRect
import ru.vkdev.dressdoll.repository.api.CatalogSection
import ru.vkdev.dressdoll.repository.api.CatalogSectionId
import ru.vkdev.dressdoll.repository.api.CategoryId
import ru.vkdev.dressdoll.repository.api.ClothingCategory
import ru.vkdev.dressdoll.repository.api.ClothingItem
import ru.vkdev.dressdoll.repository.api.ClothingItemId
import ru.vkdev.dressdoll.repository.api.DrawLevel
import ru.vkdev.dressdoll.repository.api.DrawLevelId
import ru.vkdev.dressdoll.repository.api.ReplacementGroup
import ru.vkdev.dressdoll.repository.api.ReplacementGroupId
import ru.vkdev.dressdoll.temporarydataset.CategoryName
import ru.vkdev.dressdoll.temporarydataset.TDCanvasPoint
import ru.vkdev.dressdoll.temporarydataset.TDCanvasRect
import ru.vkdev.dressdoll.temporarydataset.TDClothingCategory
import ru.vkdev.dressdoll.temporarydataset.TDClothingItem
import ru.vkdev.dressdoll.temporarydataset.TDGroup
import ru.vkdev.dressdoll.temporarydataset.TDGroupId
import ru.vkdev.dressdoll.temporarydataset.TDItemId
import ru.vkdev.dressdoll.temporarydataset.TDLevel
import ru.vkdev.dressdoll.temporarydataset.TDLevelId
import ru.vkdev.dressdoll.temporarydataset.TDSection
import ru.vkdev.dressdoll.temporarydataset.TDSectionId
import ru.vkdev.dressdoll.temporarydataset.TDSlot

class TemporaryDatasetMappersTest {

    @Test
    fun `category maps all properties`() {
        assertEquals(
            ClothingCategory(
                id = CategoryId("колготки"),
                title = "колготки",
                slot = BodySlot.WAIST,
                level = DrawLevel(DrawLevelId("tights"), 2),
                group = ReplacementGroup(ReplacementGroupId("tights"), 1),
                section = CatalogSectionId("hosiery"),
            ),
            TDClothingCategory(
                name = CategoryName("колготки"),
                slot = TDSlot.WAIST,
                level = TDLevel(TDLevelId("tights"), 2),
                group = TDGroup(TDGroupId("tights"), 1),
                section = TDSectionId("hosiery"),
            ).toClothingCategory(),
        )
    }

    @Test
    fun `every slot maps to the slot with the same name`() {
        for (slot in TDSlot.entries) {
            assertEquals(slot.name, slot.toBodySlot().name)
        }
        assertEquals(TDSlot.entries.size, BodySlot.entries.size)
    }

    @Test
    fun `group keeps limit above one`() {
        assertEquals(
            ReplacementGroup(ReplacementGroupId("undershirts"), 2),
            TDGroup(TDGroupId("undershirts"), 2).toReplacementGroup(),
        )
    }

    @Test
    fun `section maps id and title`() {
        assertEquals(
            CatalogSection(CatalogSectionId("underwear"), "Бельё"),
            TDSection(TDSectionId("underwear"), "Бельё").toCatalogSection(),
        )
    }

    @Test
    fun `levels compare by position`() {
        val socks = TDLevel(TDLevelId("socks"), 3).toDrawLevel()
        val tights = TDLevel(TDLevelId("tights"), 2).toDrawLevel()

        assertTrue(socks.compareByPosition(tights) > 0)
        assertTrue(tights.compareByPosition(socks) < 0)
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
    fun `canvas point and rect keep coordinates`() {
        assertEquals(CanvasPoint(x = -4, y = 1178), TDCanvasPoint(x = -4, y = 1178).toCanvasPoint())
        assertEquals(
            CanvasRect(left = 221, top = 66, width = 329, height = 1226),
            TDCanvasRect(left = 221, top = 66, width = 329, height = 1226).toCanvasRect(),
        )
    }

    @Test
    fun `item id round-trips to dataset id`() {
        val id = TDItemId(CategoryName("блузка"), "02")

        assertEquals(id, id.toClothingItemId().toTDItemId())
    }
}
