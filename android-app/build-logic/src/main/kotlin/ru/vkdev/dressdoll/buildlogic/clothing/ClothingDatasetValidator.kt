package ru.vkdev.dressdoll.buildlogic.clothing

import groovy.json.JsonException
import groovy.json.JsonSlurper
import java.text.Normalizer

/**
 * Проверяет инварианты из `.docs/doll/z-index.md`. Собирает все нарушения, а не падает
 * на первом: рантайм датасета доверяет этой проверке и сам ошибки не перечисляет.
 */
internal object ClothingDatasetValidator {

    /** Совпадает с `TDSlot` в `:temporary-dataset`. */
    val SLOTS = listOf("feet", "legs", "waist", "torso", "neck", "arms", "head")

    private val ROOT_FIELDS = setOf("levels", "groups", "sections", "categories")
    private val GROUP_FIELDS = setOf("limit")
    private val SECTION_FIELDS = setOf("id", "title")
    private val CATEGORY_FIELDS = setOf("slot", "level", "group", "section")

    fun validate(configJson: String, categoryFolders: Set<String>): List<String> {
        val root = try {
            JsonSlurper().parseText(configJson)
        } catch (e: JsonException) {
            return listOf("не удалось разобрать JSON: ${e.message}")
        }
        if (root !is Map<*, *>) return listOf("корень должен быть объектом")

        val errors = mutableListOf<String>()
        errors += unknownFields(root, ROOT_FIELDS, "в корне")

        val levels = parseLevels(root["levels"], errors)
        val groups = parseGroups(root["groups"], errors)
        val sections = parseSections(root["sections"], errors)
        val categories = parseCategories(root["categories"], levels, groups, sections, errors)

        checkFolders(categoryFolders, categories.map { it.name }, errors)

        val usedLevels = categories.mapNotNullTo(mutableSetOf()) { it.level }
        levels.filterNot { it in usedLevels }.forEach { errors += "уровень «$it» не используется ни одной категорией" }
        val usedGroups = categories.mapNotNullTo(mutableSetOf()) { it.group }
        groups.filterNot { it in usedGroups }.forEach { errors += "группа «$it» не используется ни одной категорией" }
        val usedSections = categories.mapNotNullTo(mutableSetOf()) { it.section }
        sections.filterNot { it in usedSections }.forEach { errors += "раздел «$it» пуст" }

        return errors
    }

    /** Поля, которые не удалось разобрать, остаются `null`: ошибка о них уже в отчёте. */
    private class Category(val name: String, val level: String?, val group: String?, val section: String?)

    private fun parseLevels(json: Any?, errors: MutableList<String>): List<String> {
        if (json !is List<*>) {
            errors += "«levels» должен быть массивом строк"
            return emptyList()
        }
        val levels = json.mapIndexedNotNull { index, id ->
            val level = (id as? String)?.takeIf { it.isNotEmpty() }
            if (level == null) errors += "уровень №${index + 1}: id должен быть непустой строкой"
            level
        }
        duplicates(levels).forEach { errors += "уровень «$it» объявлен несколько раз" }
        return levels.distinct()
    }

    // Дубликаты ключей объекта JsonSlurper молча схлопывает, поэтому уникальность групп
    // по ключу проверить нельзя: она держится на том, что id групп — латиница без вариантов написания.
    private fun parseGroups(json: Any?, errors: MutableList<String>): List<String> {
        if (json == null) return emptyList()
        if (json !is Map<*, *>) {
            errors += "«groups» должен быть объектом"
            return emptyList()
        }
        return json.mapNotNull { (key, value) ->
            val id = key as String
            if (value !is Map<*, *>) {
                errors += "группа «$id»: описание должно быть объектом"
                return@mapNotNull id
            }
            errors += unknownFields(value, GROUP_FIELDS, "группа «$id»:")
            val limit = value["limit"]
            if (limit !is Int || limit < 1) errors += "группа «$id»: limit должен быть целым числом ≥ 1, сейчас «$limit»"
            id
        }
    }

    private fun parseSections(json: Any?, errors: MutableList<String>): List<String> {
        if (json !is List<*>) {
            errors += "«sections» должен быть массивом"
            return emptyList()
        }
        val sections = json.mapIndexedNotNull { index, entry ->
            val owner = "раздел №${index + 1}"
            if (entry !is Map<*, *>) {
                errors += "$owner: описание должно быть объектом"
                return@mapIndexedNotNull null
            }
            val id = (entry["id"] as? String)?.takeIf { it.isNotEmpty() }
            val name = id?.let { "раздел «$it»" } ?: owner
            errors += unknownFields(entry, SECTION_FIELDS, "$name:")
            if (id == null) errors += "$owner: id должен быть непустой строкой"
            if ((entry["title"] as? String).isNullOrEmpty()) errors += "$name: нет названия «title»"
            id
        }
        duplicates(sections).forEach { errors += "раздел «$it» объявлен несколько раз" }
        return sections.distinct()
    }

    private fun parseCategories(
        json: Any?,
        levels: List<String>,
        groups: List<String>,
        sections: List<String>,
        errors: MutableList<String>,
    ): List<Category> {
        if (json !is Map<*, *>) {
            errors += "«categories» должен быть объектом"
            return emptyList()
        }
        val categories = json.map { (key, value) ->
            val name = key as String
            if (!name.isNfc()) errors += "ключ категории «$name» не в NFC"
            if (value !is Map<*, *>) {
                errors += "«$name»: описание должно быть объектом"
                return@map Category(name, level = null, group = null, section = null)
            }
            errors += unknownFields(value, CATEGORY_FIELDS, "«$name»:")

            val slot = requireString(value, "slot", name, errors)
            if (slot != null && slot !in SLOTS) {
                errors += "«$name»: слот «$slot» не найден, допустимы: ${SLOTS.joinToString()}"
            }
            val level = requireString(value, "level", name, errors)
                ?.let { requireKnown(it, levels, "«$name»: уровень «$it» не найден", errors) }
            val section = requireString(value, "section", name, errors)
                ?.let { requireKnown(it, sections, "«$name»: раздел «$it» не найден", errors) }
            val group = when (val raw = value["group"]) {
                null -> {
                    // Без группы категория образует собственную группу с id = имя категории.
                    if (name in groups) errors += "«$name»: неявная группа совпадает с явной группой «$name»"
                    null
                }
                is String -> requireKnown(raw, groups, "«$name»: группа «$raw» не найдена", errors)
                else -> {
                    errors += "«$name»: поле «group» должно быть строкой"
                    null
                }
            }
            Category(name, level, group, section)
        }
        duplicates(categories.map { it.name.toNfc() })
            .forEach { errors += "категория «$it» объявлена несколько раз в разных нормализациях" }
        return categories
    }

    private fun checkFolders(folders: Set<String>, categoryNames: List<String>, errors: MutableList<String>) {
        folders.filterNot { it.isNfc() }.sorted().forEach { errors += "имя папки «$it» не в NFC" }
        // Сравнение после NFC, чтобы ненормализованное имя не дало ещё и ложного расхождения.
        val folderNames = folders.mapTo(mutableSetOf()) { it.toNfc() }
        val configNames = categoryNames.mapTo(mutableSetOf()) { it.toNfc() }
        (folderNames - configNames).sorted().forEach { errors += "папка «$it» есть, записи в categories нет" }
        (configNames - folderNames).sorted().forEach { errors += "запись «$it» есть, папки в cloth-src нет" }
    }

    private fun requireString(entry: Map<*, *>, field: String, name: String, errors: MutableList<String>): String? {
        val value = (entry[field] as? String)?.takeIf { it.isNotEmpty() }
        if (value == null) errors += "«$name»: поле «$field» должно быть непустой строкой"
        return value
    }

    private fun requireKnown(id: String, known: List<String>, error: String, errors: MutableList<String>): String? {
        if (id in known) return id
        errors += error
        return null
    }

    private fun unknownFields(entry: Map<*, *>, known: Set<String>, owner: String): List<String> =
        entry.keys.map { it as String }.filterNot { it in known }.map { "$owner лишнее поле «$it»" }

    private fun duplicates(ids: List<String>): Set<String> =
        ids.groupingBy { it }.eachCount().filterValues { it > 1 }.keys

    private fun String.isNfc(): Boolean = Normalizer.isNormalized(this, Normalizer.Form.NFC)

    private fun String.toNfc(): String = Normalizer.normalize(this, Normalizer.Form.NFC)
}
