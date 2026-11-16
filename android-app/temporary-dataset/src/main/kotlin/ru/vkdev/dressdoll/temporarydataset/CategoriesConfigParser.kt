package ru.vkdev.dressdoll.temporarydataset

import org.json.JSONObject
import java.text.Normalizer

/** Лежит рядом с [DATASET_ROOT], а не внутри, чтобы не попасть в список категорий. */
internal const val CATEGORIES_CONFIG = "categories.json"

/**
 * [groups] — только явные группы, неявные есть только у категорий.
 * [categories] упорядочены по разделам, внутри раздела — как в файле.
 */
internal data class TDCategoriesConfig(
    val levels: List<TDLevel>,
    val groups: List<TDGroup>,
    val sections: List<TDSection>,
    val categories: List<TDClothingCategory>,
)

/**
 * Конфиг проверяет сборка, поэтому разбор не собирает все нарушения: падает на первом
 * и не подставляет значения по умолчанию.
 */
internal object CategoriesConfigParser {

    fun parse(json: String): TDCategoriesConfig {
        val root = JSONObject(json)
        val levels = parseLevels(root)
        val groups = parseGroups(root)
        val sections = parseSections(root)

        val levelsById = levels.associateBy { it.id.value }
        val groupsById = groups.associateBy { it.id.value }
        val sectionIndex = sections.withIndex().associate { (index, section) -> section.id.value to index }

        val categoriesJson = root.getJSONObject("categories")
        val categories = categoriesJson.keys().asSequence().map { key ->
            val name = key.toNfc()
            val entry = categoriesJson.getJSONObject(key)
            val owner = "Category '$name'"
            val slot = entry.requireString("slot", owner)
            val level = entry.requireString("level", owner)
            val section = entry.requireString("section", owner)
            val group = entry.optString("group").takeIf { it.isNotEmpty() }
            TDClothingCategory(
                name = CategoryName(name),
                slot = TDSlot.entries.firstOrNull { it.id == slot }
                    ?: error("Category '$name': unknown slot '$slot'"),
                level = levelsById[level] ?: error("Category '$name': unknown level '$level'"),
                group = if (group == null) {
                    TDGroup(TDGroupId(name), limit = 1)
                } else {
                    groupsById[group] ?: error("Category '$name': unknown group '$group'")
                },
                section = TDSectionId(section).also {
                    check(section in sectionIndex) { "Category '$name': unknown section '$section'" }
                },
            )
        }.toList()
        // Ключи JSON уникальны, но написания одного имени в NFD и NFC совпадают после нормализации.
        categories.checkUniqueIds("category") { it.name.value }

        return TDCategoriesConfig(
            levels = levels,
            groups = groups,
            sections = sections,
            // sortedBy стабилен: внутри раздела сохраняется порядок из файла.
            categories = categories.sortedBy { sectionIndex.getValue(it.section.value) },
        )
    }

    private fun parseLevels(root: JSONObject): List<TDLevel> {
        val array = root.getJSONArray("levels")
        val levels = (0 until array.length()).map { index ->
            TDLevel(TDLevelId(array.getString(index)), position = index + 1)
        }
        levels.checkUniqueIds("level") { it.id.value }
        return levels
    }

    private fun parseGroups(root: JSONObject): List<TDGroup> {
        val json = root.optJSONObject("groups") ?: return emptyList()
        return json.keys().asSequence().map { id ->
            val limit = json.getJSONObject(id).getInt("limit")
            check(limit >= 1) { "Group '$id': limit $limit < 1" }
            TDGroup(TDGroupId(id), limit)
        }.toList()
    }

    private fun parseSections(root: JSONObject): List<TDSection> {
        val array = root.getJSONArray("sections")
        val sections = (0 until array.length()).map { index ->
            val entry = array.getJSONObject(index)
            val id = entry.getString("id")
            TDSection(TDSectionId(id), title = entry.requireString("title", "Section '$id'"))
        }
        sections.checkUniqueIds("section") { it.id.value }
        return sections
    }

    private fun JSONObject.requireString(field: String, owner: String): String =
        optString(field).takeIf { it.isNotEmpty() } ?: error("$owner: no '$field'")

    private fun <T> List<T>.checkUniqueIds(kind: String, id: (T) -> String) {
        val duplicate = groupingBy(id).eachCount().filterValues { it > 1 }.keys.firstOrNull()
        check(duplicate == null) { "Duplicate $kind '$duplicate'" }
    }
}

internal fun String.toNfc(): String = Normalizer.normalize(this, Normalizer.Form.NFC)
