package ru.vkdev.dressdoll.outfit.impl

import kotlinx.serialization.Serializable

/** [version] — для будущих миграций формата. */
@Serializable
internal data class OutfitFile(
    val version: Int = 1,
    val worn: List<StoredItem> = emptyList(),
)

@Serializable
internal data class StoredItem(val category: String, val key: String)
