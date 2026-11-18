package ru.vkdev.dressdoll.doll.impl

import ru.vkdev.dressdoll.clothing.api.BodySlot
import ru.vkdev.dressdoll.clothing.api.CategoryId
import ru.vkdev.dressdoll.clothing.api.ClothingCategory
import ru.vkdev.dressdoll.clothing.api.DrawLevel
import ru.vkdev.dressdoll.clothing.api.DrawLevelId
import ru.vkdev.dressdoll.clothing.api.ReplacementGroup
import ru.vkdev.dressdoll.clothing.api.ReplacementGroupId

// Уровни и группы — подмножество categories.json, позиции уровней как в z-index.md.
private val UNDERWEAR = level("underwear", 1)
private val TIGHTS_LEVEL = level("tights", 2)
private val SOCKS_LEVEL = level("socks", 3)
private val UNDERSHIRTS_LEVEL = level("undershirts", 6)
private val SHIRTS = level("shirts", 7)
private val JACKETS = level("jackets", 12)

internal val UNDERSHIRTS = ReplacementGroup(ReplacementGroupId("undershirts"), limit = 2)
private val TIGHTS_GROUP = ReplacementGroup(ReplacementGroupId("tights"), limit = 1)
private val JACKETS_GROUP = ReplacementGroup(ReplacementGroupId("jackets"), limit = 1)

internal val CATEGORIES: Map<CategoryId, ClothingCategory> = listOf(
    category("трусы", UNDERWEAR, ownGroup("трусы")),
    category("бюстгальтер", UNDERWEAR, ownGroup("бюстгальтер")),
    category("колготки", TIGHTS_LEVEL, TIGHTS_GROUP, BodySlot.LEGS),
    category("чулки", TIGHTS_LEVEL, TIGHTS_GROUP, BodySlot.LEGS),
    category("носки", SOCKS_LEVEL, ownGroup("носки")),
    category("майка", UNDERSHIRTS_LEVEL, UNDERSHIRTS),
    category("футболка", UNDERSHIRTS_LEVEL, UNDERSHIRTS),
    category("водолазка", UNDERSHIRTS_LEVEL, UNDERSHIRTS),
    category("рубашка", SHIRTS, ownGroup("рубашка")),
    category("пиджак", JACKETS, JACKETS_GROUP),
    category("жакет", JACKETS, JACKETS_GROUP),
).associateBy { it.id }

private fun level(id: String, position: Int) = DrawLevel(DrawLevelId(id), position)

private fun ownGroup(category: String) = ReplacementGroup(ReplacementGroupId(category), limit = 1)

private fun category(
    name: String,
    level: DrawLevel,
    group: ReplacementGroup,
    slot: BodySlot = BodySlot.TORSO,
) = ClothingCategory(
    id = CategoryId(name),
    title = name,
    slot = slot,
    level = level,
    group = group,
)
