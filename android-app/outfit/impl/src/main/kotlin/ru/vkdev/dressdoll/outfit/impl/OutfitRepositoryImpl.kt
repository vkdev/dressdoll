package ru.vkdev.dressdoll.outfit.impl

import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import ru.vkdev.dressdoll.core.datastore.PersistedValue
import ru.vkdev.dressdoll.outfit.api.OutfitRepository
import ru.vkdev.dressdoll.outfit.api.WornItem
import ru.vkdev.dressdoll.repository.api.CategoryId
import ru.vkdev.dressdoll.repository.api.ClothingItemId

internal class OutfitRepositoryImpl(
    dataStore: DataStore<OutfitFile>,
    scope: CoroutineScope,
) : OutfitRepository {

    private val persisted = PersistedValue<OutfitFile, List<WornItem>?>(
        dataStore = dataStore,
        scope = scope,
        initial = null,
        onReadError = OutfitFile(),
        read = { file -> file.worn.map { it.toWornItem() } },
        write = { file, worn -> file.copy(worn = worn.orEmpty().map { it.toStored() }) },
    )

    override val worn: StateFlow<List<WornItem>?> = persisted.value

    override fun save(worn: List<WornItem>) = persisted.set(worn)

    private fun StoredItem.toWornItem() = WornItem(ClothingItemId(CategoryId(category), key))

    private fun WornItem.toStored() = StoredItem(category = id.category.value, key = id.key)
}
