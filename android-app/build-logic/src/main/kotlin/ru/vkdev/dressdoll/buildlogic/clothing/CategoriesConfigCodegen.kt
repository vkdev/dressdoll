package ru.vkdev.dressdoll.buildlogic.clothing

internal object CategoriesConfigCodegen {

    const val PACKAGE = "ru.vkdev.dressdoll.temporarydataset"
    const val FILE_NAME = "CategoriesConfig.kt"

    fun generate(config: DatasetConfig): String = buildString {
        // Уровни и явные группы — общие объекты, на которые ссылаются категории,
        // поэтому они вынесены в отдельные val с именами по индексу: id не обязаны быть идентификаторами.
        val levelRefs = config.levels.withIndex().associate { (index, id) -> id to "level${index + 1}" }
        val groupRefs = config.groups.withIndex().associate { (index, group) -> group.id to "group${index + 1}" }

        appendLine("// Сгенерировано из categories.json задачей generateCategoriesConfig, не редактировать.")
        appendLine("package $PACKAGE")
        appendLine()
        config.levels.forEachIndexed { index, id ->
            appendLine("private val ${levelRefs.getValue(id)} = TDLevel(TDLevelId(${literal(id)}), position = ${index + 1})")
        }
        appendLine()
        config.groups.forEach { group ->
            appendLine("private val ${groupRefs.getValue(group.id)} = TDGroup(TDGroupId(${literal(group.id)}), limit = ${group.limit})")
        }
        appendLine()
        appendLine("internal val categoriesConfig = TDCategoriesConfig(")
        appendLine("    levels = listOf(${config.levels.joinToString { levelRefs.getValue(it) }}),")
        appendLine("    groups = listOf(${config.groups.joinToString { groupRefs.getValue(it.id) }}),")
        appendLine("    sections = listOf(")
        config.sections.forEach { section ->
            appendLine("        TDSection(TDSectionId(${literal(section.id)}), title = ${literal(section.title)}),")
        }
        appendLine("    ),")
        appendLine("    categories = listOf(")
        config.categories.forEach { category ->
            val group = category.group?.let(groupRefs::getValue)
                ?: "TDGroup(TDGroupId(${literal(category.name)}), limit = 1)"
            appendLine("        TDClothingCategory(")
            appendLine("            name = CategoryName(${literal(category.name)}),")
            appendLine("            slot = TDSlot.${category.slot.uppercase()},")
            appendLine("            level = ${levelRefs.getValue(category.level)},")
            appendLine("            group = $group,")
            appendLine("            section = TDSectionId(${literal(category.section)}),")
            appendLine("        ),")
        }
        appendLine("    ),")
        appendLine(")")
    }

    private fun literal(value: String): String = buildString {
        append('"')
        value.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '$' -> append("\\$")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(char)
            }
        }
        append('"')
    }
}
