package ru.vkdev.dressdoll.doll.impl

import ru.vkdev.dressdoll.outfit.api.WornItem
import ru.vkdev.dressdoll.repository.api.CategoryId
import ru.vkdev.dressdoll.repository.api.ClothingCategory
import ru.vkdev.dressdoll.repository.api.ClothingItemId

// Правила из .docs/doll/z-index.md. Образ — список в порядке надевания: раньше надетая вещь ближе к началу.

/**
 * Надевает [item] последним. Если лимит группы исчерпан, снимается самая ранняя вещь группы.
 * Уже надетая вещь переносится в конец.
 */
internal fun wear(worn: List<WornItem>, item: WornItem, categories: Map<CategoryId, ClothingCategory>): List<WornItem> {
    val group = categories.getValue(item.id.category).group
    val rest = worn.filterNot { it.id == item.id }
    val sameGroup = rest.filter { categories.getValue(it.id.category).group.id == group.id }
    // Группа уже полна: вытесняется столько самых ранних, чтобы с новой вещью уложиться в лимит.
    val evicted = sameGroup.take((sameGroup.size - group.limit + 1).coerceAtLeast(0)).toSet()
    return rest.filterNot { it in evicted } + item
}

/** Снимает надетую вещь, иначе надевает её по [wear]. */
internal fun toggle(worn: List<WornItem>, item: ClothingItemId, categories: Map<CategoryId, ClothingCategory>): List<WornItem> =
    if (worn.any { it.id == item }) {
        worn.filterNot { it.id == item }
    } else {
        wear(worn, WornItem(item), categories)
    }

/**
 * Приводит сохранённый образ к текущему датасету: отбрасывает неизвестные вещи и повторы
 * (остаётся первое вхождение), остальные надевает заново по порядку.
 *
 * Через [wear], а не [toggle]: повтор вещи снял бы её.
 */
internal fun normalize(
    saved: List<WornItem>,
    categories: Map<CategoryId, ClothingCategory>,
    items: Set<ClothingItemId>,
): List<WornItem> =
    saved
        .filter { it.id in items && it.id.category in categories }
        .distinctBy { it.id }
        .fold(emptyList()) { worn, item -> wear(worn, item, categories) }

/** Снизу вверх: по позиции уровня, внутри уровня — в порядке надевания. */
internal fun drawOrder(worn: List<WornItem>, categories: Map<CategoryId, ClothingCategory>): List<WornItem> =
    // sortedWith стабилен: внутри уровня сохраняется порядок надевания.
    worn.sortedWith { a, b ->
        categories.getValue(a.id.category).level.compareByPosition(categories.getValue(b.id.category).level)
    }
