package ru.vkdev.dressdoll.temporarydataset

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.text.Normalizer

// org.json в android.jar — заглушка. Robolectric подставляет реализацию Android:
// порядок ключей (порядок категорий в файле) держит только она.
@RunWith(RobolectricTestRunner::class)
class CategoriesConfigParserTest {

    @Test
    fun `parses levels, groups, sections and categories`() {
        val config = CategoriesConfigParser.parse(config())

        assertEquals(
            listOf(TDLevel(TDLevelId("underwear"), 1), TDLevel(TDLevelId("tights"), 2)),
            config.levels,
        )
        assertEquals(listOf(TDGroup(TDGroupId("tights"), 1)), config.groups)
        assertEquals(
            listOf(TDSection(TDSectionId("underwear"), "Бельё"), TDSection(TDSectionId("hosiery"), "Носки и колготки")),
            config.sections,
        )
        assertEquals(
            TDClothingCategory(
                name = CategoryName("чулки"),
                slot = TDSlot.LEGS,
                level = TDLevel(TDLevelId("tights"), 2),
                group = TDGroup(TDGroupId("tights"), 1),
                section = TDSectionId("hosiery"),
            ),
            config.categories.single { it.name.value == "чулки" },
        )
    }

    @Test
    fun `categories are ordered by section, then by file order`() {
        val config = CategoriesConfigParser.parse(config())

        assertEquals(listOf("трусы", "чулки", "колготки"), config.categories.map { it.name.value })
    }

    @Test
    fun `category without group gets its own group with limit 1`() {
        val config = CategoriesConfigParser.parse(config())

        assertEquals(TDGroup(TDGroupId("трусы"), 1), config.categories.first().group)
    }

    @Test
    fun `category names are normalized to NFC`() {
        val nfd = Normalizer.normalize("бельевая-майка", Normalizer.Form.NFD)
        val config = CategoriesConfigParser.parse(
            config(categories = """"$nfd": { "slot": "torso", "level": "underwear", "section": "underwear" }"""),
        )

        assertEquals("бельевая-майка", config.categories.single().name.value)
    }

    @Test
    fun `unknown level fails with category name`() {
        assertFailsFor("трусы", """"трусы": { "slot": "waist", "level": "nope", "section": "underwear" }""")
    }

    @Test
    fun `unknown group fails with category name`() {
        assertFailsFor(
            "трусы",
            """"трусы": { "slot": "waist", "level": "underwear", "group": "nope", "section": "underwear" }""",
        )
    }

    @Test
    fun `unknown section fails with category name`() {
        assertFailsFor("трусы", """"трусы": { "slot": "waist", "level": "underwear", "section": "nope" }""")
    }

    @Test
    fun `unknown slot fails with category name`() {
        assertFailsFor("трусы", """"трусы": { "slot": "tail", "level": "underwear", "section": "underwear" }""")
    }

    @Test
    fun `duplicate level fails`() {
        val error = assertThrows(IllegalStateException::class.java) {
            CategoriesConfigParser.parse(config(levels = """"underwear", "tights", "underwear""""))
        }

        assertTrue(error.message, "underwear" in error.message.orEmpty())
    }

    private fun assertFailsFor(category: String, categories: String) {
        val error = assertThrows(IllegalStateException::class.java) {
            CategoriesConfigParser.parse(config(categories = categories))
        }
        assertTrue(error.message, category in error.message.orEmpty())
    }

    private fun config(
        levels: String = """"underwear", "tights"""",
        categories: String = """
            "чулки": { "slot": "legs", "level": "tights", "group": "tights", "section": "hosiery" },
            "трусы": { "slot": "waist", "level": "underwear", "section": "underwear" },
            "колготки": { "slot": "waist", "level": "tights", "group": "tights", "section": "hosiery" }
        """,
    ) = """
        {
          "levels": [$levels],
          "groups": { "tights": { "limit": 1 } },
          "sections": [
            { "id": "underwear", "title": "Бельё" },
            { "id": "hosiery", "title": "Носки и колготки" }
          ],
          "categories": { $categories }
        }
    """
}
