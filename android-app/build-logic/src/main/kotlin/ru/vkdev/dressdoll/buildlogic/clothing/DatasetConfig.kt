package ru.vkdev.dressdoll.buildlogic.clothing

/**
 * Конфиг после проверки: ссылки существуют, [categories] упорядочены по разделам,
 * внутри раздела — как в файле. Позиция уровня — его индекс в [levels] плюс 1.
 */
internal data class DatasetConfig(
    val levels: List<String>,
    val groups: List<DatasetGroup>,
    val sections: List<DatasetSection>,
    val categories: List<DatasetCategory>,
)

internal data class DatasetGroup(val id: String, val limit: Int)

internal data class DatasetSection(val id: String, val title: String)

/** [group] — `null`, если у категории неявная группа: id = [name], лимит 1. */
internal data class DatasetCategory(
    val name: String,
    val slot: String,
    val level: String,
    val group: String?,
    val section: String,
)

/** [config] есть только при пустом [errors]. */
internal class ValidationResult(val errors: List<String>, val config: DatasetConfig?)
