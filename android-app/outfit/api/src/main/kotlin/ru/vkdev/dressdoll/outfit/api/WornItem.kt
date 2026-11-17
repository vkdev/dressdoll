package ru.vkdev.dressdoll.outfit.api

import ru.vkdev.dressdoll.repository.api.ClothingItemId

/** Обёртка, а не голый [ClothingItemId]: сюда добавятся режим «заправлен» и крепление к вещи. */
data class WornItem(val id: ClothingItemId)
