package ru.vkdev.dressdoll.buildlogic.clothing

import com.fasterxml.jackson.core.JsonProcessingException
import java.text.Normalizer

/**
 * Проверяет инварианты из `.docs/doll/z-index.md`. Собирает все нарушения, а не падает
 * на первом: конфиг для рантайма генерируется только из проверенного файла.
 */
internal object ClothingDatasetValidator {

    /** Должны совпадать с константами `TDSlot`: на них ссылается сгенерированный код. */
    val SLOTS = listOf("feet", "legs", "waist", "torso", "neck", "arms", "head")

    private val ROOT_FIELDS = setOf("levels", "groups", "sections", "categories")
    private val GROUP_FIELDS = setOf("limit")
    private val SECTION_FIELDS = setOf("id", "title")
    private val CATEGORY_FIELDS = setOf("slot", "level", "group", "section")

    fun validate(
        configJson: String,
        categoryFolders: Set<String>,
        strayFiles: Set<String> = emptySet(),
    ): ValidationResult {
        val tree = try {
            readJsonTree(configJson)
        } catch (e: JsonProcessingException) {
            return failure("не удалось разобрать JSON: ${e.originalMessage}")
        }
        val root = tree.root as? Map<*, *> ?: return failure("корень должен быть объектом")

        val errors = mutableListOf<String>()
        tree.duplicateKeys.forEach { (path, key) ->
            val owner = if (path.isEmpty()) "в корне" else "в «${path.joinToString(".")}»"
            errors += "$owner ключ «$key» повторяется"
        }
        errors += unknownFields(root, ROOT_FIELDS, "в корне")

        val levels = parseLevels(root["levels"], errors)
        val groups = parseGroups(root["groups"], errors)
        val sections = parseSections(root["sections"], errors)
        val categories = parseCategories(root["categories"], levels, groups.map { it.id }, sections.map { it.id }, errors)

        checkFolders(categoryFolders, categories.map { it.name }, errors)
        strayFiles.sorted().forEach { errors += "файл «$it» лежит в cloth-src: там должны быть только папки категорий" }

        val usedLevels = categories.mapNotNullTo(mutableSetOf()) { it.level }
        levels.filterNot { it in usedLevels }.forEach { errors += "уровень «$it» не используется ни одной категорией" }
        val usedGroups = categories.mapNotNullTo(mutableSetOf()) { it.group }
        groups.filterNot { it.id in usedGroups }.forEach { errors += "группа «${it.id}» не используется ни одной категорией" }
        val usedSections = categories.mapNotNullTo(mutableSetOf()) { it.section }
        sections.filterNot { it.id in usedSections }.forEach { errors += "раздел «${it.id}» пуст" }

        if (errors.isNotEmpty()) return ValidationResult(errors, config = null)

        // Без ошибок все поля разобраны: null остаётся только рядом с ошибкой в отчёте.
        val sectionOrder = sections.map { it.id }
        val config = DatasetConfig(
            levels = levels,
            groups = groups.map { DatasetGroup(it.id, checkNotNull(it.limit)) },
            sections = sections.map { DatasetSection(it.id, checkNotNull(it.title)) },
            categories = categories
                .map {
                    DatasetCategory(
                        name = it.name,
                        slot = checkNotNull(it.slot),
                        level = checkNotNull(it.level),
                        group = it.group,
                        section = checkNotNull(it.section),
                    )
                }
                // sortedBy стабилен: внутри раздела сохраняется порядок из файла.
                .sortedBy { sectionOrder.indexOf(it.section) },
        )
        return ValidationResult(errors = emptyList(), config = config)
    }

    /** Поля, которые не удалось разобрать, остаются `null`: ошибка о них уже в отчёте. */
    private class ParsedGroup(val id: String, val limit: Int?)

    private class ParsedSection(val id: String, val title: String?)

    /** [group] `null` и для неявной группы, и для ошибочного поля. */
    private class ParsedCategory(
        val name: String,
        val slot: String?,
        val level: String?,
        val group: String?,
        val section: String?,
    )

    private fun failure(error: String) = ValidationResult(listOf(error), config = null)

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

    private fun parseGroups(json: Any?, errors: MutableList<String>): List<ParsedGroup> {
        if (json == null) return emptyList()
        if (json !is Map<*, *>) {
            errors += "«groups» должен быть объектом"
            return emptyList()
        }
        return json.map { (key, value) ->
            val id = key as String
            if (value !is Map<*, *>) {
                errors += "группа «$id»: описание должно быть объектом"
                return@map ParsedGroup(id, limit = null)
            }
            errors += unknownFields(value, GROUP_FIELDS, "группа «$id»:")
            val limit = (value["limit"] as? Int)?.takeIf { it >= 1 }
            if (limit == null) errors += "группа «$id»: limit должен быть целым числом ≥ 1, сейчас «${value["limit"]}»"
            ParsedGroup(id, limit)
        }
    }

    private fun parseSections(json: Any?, errors: MutableList<String>): List<ParsedSection> {
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
            val title = (entry["title"] as? String)?.takeIf { it.isNotEmpty() }
            if (title == null) errors += "$name: нет названия «title»"
            id?.let { ParsedSection(it, title) }
        }
        duplicates(sections.map { it.id }).forEach { errors += "раздел «$it» объявлен несколько раз" }
        return sections.distinctBy { it.id }
    }

    private fun parseCategories(
        json: Any?,
        levels: List<String>,
        groups: List<String>,
        sections: List<String>,
        errors: MutableList<String>,
    ): List<ParsedCategory> {
        if (json !is Map<*, *>) {
            errors += "«categories» должен быть объектом"
            return emptyList()
        }
        val categories = json.map { (key, value) ->
            val name = key as String
            if (!name.isNfc()) errors += "ключ категории «$name» не в NFC"
            if (value !is Map<*, *>) {
                errors += "«$name»: описание должно быть объектом"
                return@map ParsedCategory(name, slot = null, level = null, group = null, section = null)
            }
            errors += unknownFields(value, CATEGORY_FIELDS, "«$name»:")

            val slot = requireString(value, "slot", name, errors)
                ?.let { requireKnown(it, SLOTS, "«$name»: слот «$it» не найден, допустимы: ${SLOTS.joinToString()}", errors) }
            val level = requireString(value, "level", name, errors)
                ?.let { requireKnown(it, levels, "«$name»: уровень «$it» не найден", errors) }
            val section = requireString(value, "section", name, errors)
                ?.let { requireKnown(it, sections, "«$name»: раздел «$it» не найден", errors) }
            val group = when {
                "group" !in value -> {
                    // Без группы категория образует собственную группу с id = имя категории.
                    if (name in groups) errors += "«$name»: неявная группа совпадает с явной группой «$name»"
                    null
                }
                // Явный null — не синоним отсутствия поля: у неявной группы один способ записи.
                else -> when (val raw = value["group"]) {
                    is String -> requireKnown(raw, groups, "«$name»: группа «$raw» не найдена", errors)
                    else -> {
                        errors += "«$name»: поле «group» должно быть строкой, сейчас «$raw»; " +
                            "для собственной группы поле не указывают"
                        null
                    }
                }
            }
            ParsedCategory(name, slot, level, group, section)
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
