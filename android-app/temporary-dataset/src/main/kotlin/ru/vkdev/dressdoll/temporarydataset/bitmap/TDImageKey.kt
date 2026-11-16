package ru.vkdev.dressdoll.temporarydataset.bitmap

import ru.vkdev.dressdoll.temporarydataset.DATASET_ROOT
import ru.vkdev.dressdoll.temporarydataset.TDImageKind
import ru.vkdev.dressdoll.temporarydataset.TDItemId

/** Ключ кеша: одна картинка одной вещи. */
internal data class TDImageKey(
    val item: TDItemId,
    val kind: TDImageKind,
) {
    val assetPath: String
        get() = "$DATASET_ROOT/${item.category.value}/${item.folder}/${kind.fileName}"
}
