package ru.vkdev.dressdoll.catalog.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.vkdev.dressdoll.repository.api.ClothingItemId

interface Catalog {

    /**
     * Содержимое шторки: категории или вещи одной категории.
     *
     * @param isOpen шторка открыта и каталог виден. Пока `false`, системный «Назад»
     * не закрывает категорию: шторка держит содержимое в композиции и скрытой.
     */
    @Composable
    fun Content(
        selected: ClothingItemId?,
        onSelect: (ClothingItemId) -> Unit,
        isOpen: Boolean,
        modifier: Modifier = Modifier,
    )
}
