package ru.vkdev.dressdoll.catalog.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.vkdev.dressdoll.clothing.api.ClothingItemId

interface Catalog {

    /**
     * Содержимое шторки: категории или вещи одной категории.
     *
     * @param worn надетые вещи, отмечаются в каталоге; `null`, пока образ не прочитан.
     * @param onToggle нажатие на вещь: надеть или снять решает вызывающий.
     * @param isOpen шторка открыта и каталог виден. Пока `false`, системный «Назад»
     * не закрывает категорию: шторка держит содержимое в композиции и скрытой.
     */
    @Composable
    fun Content(
        worn: Set<ClothingItemId>?,
        onToggle: (ClothingItemId) -> Unit,
        isOpen: Boolean,
        modifier: Modifier = Modifier,
    )
}
