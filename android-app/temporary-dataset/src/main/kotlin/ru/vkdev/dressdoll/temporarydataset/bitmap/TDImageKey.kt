package ru.vkdev.dressdoll.temporarydataset.bitmap

import ru.vkdev.dressdoll.temporarydataset.DATASET_ROOT
import ru.vkdev.dressdoll.temporarydataset.DOLL_IMAGE
import ru.vkdev.dressdoll.temporarydataset.TDImageKind
import ru.vkdev.dressdoll.temporarydataset.TDItemId

/** Ключ кеша: одна картинка датасета. */
internal sealed interface TDImageKey {

    val assetPath: String

    /** Картинка рисуется на кукле: лежит в кеше куклы и масштабируется вместе с ней. */
    val onDoll: Boolean

    /** Одна картинка одной вещи. */
    data class Item(val item: TDItemId, val kind: TDImageKind) : TDImageKey {
        override val assetPath: String
            get() = "$DATASET_ROOT/${item.category.value}/${item.folder}/${kind.fileName}"

        override val onDoll: Boolean
            get() = kind != TDImageKind.CATALOG
    }

    data object Figure : TDImageKey {
        override val assetPath: String = DOLL_IMAGE
        override val onDoll: Boolean = true
    }
}
