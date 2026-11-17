package ru.vkdev.dressdoll.buildlogic.clothing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoriesConfigCodegenTest {

    @Test
    fun `генерирует конфиг с общими уровнями и группами`() {
        val config = DatasetConfig(
            levels = listOf("underwear", "tights"),
            groups = listOf(DatasetGroup("tights", 1)),
            sections = listOf(DatasetSection("underwear", "Бельё"), DatasetSection("hosiery", "Носки и колготки")),
            categories = listOf(
                DatasetCategory("трусы", slot = "waist", level = "underwear", group = null, section = "underwear"),
                DatasetCategory("чулки", slot = "legs", level = "tights", group = "tights", section = "hosiery"),
            ),
        )

        assertEquals(
            """
            // Сгенерировано из categories.json задачей generateDatasetConfig, не редактировать.
            package ru.vkdev.dressdoll.temporarydataset

            private val level1 = TDLevel(TDLevelId("underwear"), position = 1)
            private val level2 = TDLevel(TDLevelId("tights"), position = 2)

            private val group1 = TDGroup(TDGroupId("tights"), limit = 1)

            internal val categoriesConfig = TDCategoriesConfig(
                levels = listOf(level1, level2),
                groups = listOf(group1),
                sections = listOf(
                    TDSection(TDSectionId("underwear"), title = "Бельё"),
                    TDSection(TDSectionId("hosiery"), title = "Носки и колготки"),
                ),
                categories = listOf(
                    TDClothingCategory(
                        name = CategoryName("трусы"),
                        slot = TDSlot.WAIST,
                        level = level1,
                        group = TDGroup(TDGroupId("трусы"), limit = 1),
                        section = TDSectionId("underwear"),
                    ),
                    TDClothingCategory(
                        name = CategoryName("чулки"),
                        slot = TDSlot.LEGS,
                        level = level2,
                        group = group1,
                        section = TDSectionId("hosiery"),
                    ),
                ),
            )

            """.trimIndent(),
            CategoriesConfigCodegen.generate(config),
        )
    }

    @Test
    fun `экранирует кавычки и шаблоны в строках`() {
        val config = DatasetConfig(
            levels = listOf("l"),
            groups = emptyList(),
            sections = listOf(DatasetSection("s", "Цена \$5 \"со скидкой\" \\ всё")),
            categories = listOf(DatasetCategory("c", slot = "head", level = "l", group = null, section = "s")),
        )

        val code = CategoriesConfigCodegen.generate(config)

        assertTrue(code, "title = \"Цена \\\$5 \\\"со скидкой\\\" \\\\ всё\"" in code)
    }
}
