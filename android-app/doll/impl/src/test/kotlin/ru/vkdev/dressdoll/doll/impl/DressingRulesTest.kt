package ru.vkdev.dressdoll.doll.impl

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.vkdev.dressdoll.clothing.api.CategoryId
import ru.vkdev.dressdoll.clothing.api.ClothingItemId
import ru.vkdev.dressdoll.outfit.api.WornItem

// Примеры из .docs/doll/z-index.md.
class DressingRulesTest {

    @Test
    fun `надеть и снять`() {
        val worn = toggle(emptyList(), TSHIRT, CATEGORIES)
        assertEquals(listOf(TSHIRT), worn.ids())

        assertEquals(emptyList<ClothingItemId>(), toggle(worn, TSHIRT, CATEGORIES).ids())
    }

    @Test
    fun `снятие не меняет порядок остальных`() {
        val worn = dress(PANTIES, TSHIRT, SHIRT)

        assertEquals(listOf(PANTIES, SHIRT), toggle(worn, TSHIRT, CATEGORIES).ids())
    }

    @Test
    fun `при лимите 1 новая вещь заменяет надетую`() {
        assertEquals(listOf(STOCKINGS), dress(TIGHTS, STOCKINGS).ids())
        assertEquals(listOf(BLAZER_2), dress(BLAZER, BLAZER_2).ids())
    }

    @Test
    fun `две вещи нательного при лимите 2, третья заменяет самую раннюю`() {
        assertEquals(listOf(SINGLET, TSHIRT), dress(SINGLET, TSHIRT).ids())
        assertEquals(listOf(TSHIRT, TURTLENECK), dress(SINGLET, TSHIRT, TURTLENECK).ids())
    }

    @Test
    fun `трусы и бюстгальтер одного уровня не заменяют друг друга`() {
        assertEquals(listOf(PANTIES, BRA), dress(PANTIES, BRA).ids())
    }

    @Test
    fun `вещи разных групп не заменяют друг друга`() {
        assertEquals(listOf(TSHIRT, SHIRT, BLAZER), dress(TSHIRT, SHIRT, BLAZER).ids())
    }

    @Test
    fun `повторное надевание кладёт вещь наверх своего уровня`() {
        val worn = dress(TSHIRT, TURTLENECK)
        val again = wear(worn, WornItem(TSHIRT), CATEGORIES)

        assertEquals(listOf(TURTLENECK, TSHIRT), again.ids())
        assertEquals(listOf(TURTLENECK, TSHIRT), drawOrder(again, CATEGORIES).ids())
    }

    @Test
    fun `повторное надевание при полной группе ничего не вытесняет`() {
        assertEquals(listOf(TURTLENECK, TSHIRT), wear(dress(TSHIRT, TURTLENECK), WornItem(TSHIRT), CATEGORIES).ids())
    }

    @Test
    fun `внутри уровня — порядок надевания`() {
        assertEquals(listOf(TSHIRT, TURTLENECK), drawOrder(dress(TSHIRT, TURTLENECK), CATEGORIES).ids())
        assertEquals(listOf(TURTLENECK, TSHIRT), drawOrder(dress(TURTLENECK, TSHIRT), CATEGORIES).ids())
    }

    @Test
    fun `рубашка под пиджаком независимо от порядка надевания`() {
        assertEquals(listOf(SHIRT, BLAZER), drawOrder(dress(BLAZER, SHIRT), CATEGORIES).ids())
        assertEquals(listOf(SHIRT, BLAZER), drawOrder(dress(SHIRT, BLAZER), CATEGORIES).ids())
    }

    @Test
    fun `уровень сильнее порядка надевания`() {
        assertEquals(listOf(TSHIRT, SHIRT), drawOrder(dress(SHIRT, TSHIRT), CATEGORIES).ids())
        assertEquals(listOf(TIGHTS, SOCKS), drawOrder(dress(SOCKS, TIGHTS), CATEGORIES).ids())
    }

    @Test
    fun `normalize оставляет корректный образ как есть`() {
        val worn = dress(PANTIES, TSHIRT, TURTLENECK, SHIRT)

        assertEquals(worn, normalize(worn, CATEGORIES, ITEMS))
    }

    @Test
    fun `normalize отбрасывает исчезнувшую вещь`() {
        val gone = ClothingItemId(TSHIRT.category, "99")

        assertEquals(listOf(PANTIES, SHIRT), normalize(listOf(PANTIES, gone, SHIRT).worn(), CATEGORIES, ITEMS).ids())
    }

    @Test
    fun `normalize отбрасывает вещь исчезнувшей категории`() {
        val categories = CATEGORIES - SHIRT.category

        assertEquals(listOf(PANTIES), normalize(listOf(PANTIES, SHIRT).worn(), categories, ITEMS).ids())
    }

    @Test
    fun `normalize применяет уменьшенный лимит`() {
        val saved = listOf(SINGLET, TSHIRT, TURTLENECK).worn()
        val categories = CATEGORIES.mapValues { (_, category) ->
            if (category.group.id == UNDERSHIRTS.id) category.copy(group = UNDERSHIRTS.copy(limit = 1)) else category
        }

        assertEquals(listOf(TURTLENECK), normalize(saved, categories, ITEMS).ids())
    }

    @Test
    fun `normalize оставляет первое вхождение повтора`() {
        val saved = listOf(TSHIRT, TURTLENECK, TSHIRT).worn()

        assertEquals(listOf(TSHIRT, TURTLENECK), normalize(saved, CATEGORIES, ITEMS).ids())
    }

    private fun dress(vararg items: ClothingItemId): List<WornItem> =
        items.fold(emptyList()) { worn, item -> toggle(worn, item, CATEGORIES) }

    private fun List<WornItem>.ids(): List<ClothingItemId> = map { it.id }

    private fun List<ClothingItemId>.worn(): List<WornItem> = map(::WornItem)

    private companion object {
        val PANTIES = item("трусы")
        val BRA = item("бюстгальтер")
        val TIGHTS = item("колготки")
        val STOCKINGS = item("чулки")
        val SOCKS = item("носки")
        val SINGLET = item("майка")
        val TSHIRT = item("футболка")
        val TURTLENECK = item("водолазка")
        val SHIRT = item("рубашка")
        val BLAZER = item("пиджак")
        val BLAZER_2 = item("жакет")

        val ITEMS = setOf(PANTIES, BRA, TIGHTS, STOCKINGS, SOCKS, SINGLET, TSHIRT, TURTLENECK, SHIRT, BLAZER, BLAZER_2)

        fun item(category: String) = ClothingItemId(CategoryId(category), "01")
    }
}
