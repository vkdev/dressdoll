package ru.vkdev.dressdoll.clothing.impl

import ru.vkdev.dressdoll.temporarydataset.TemporaryDatasetConfig

internal object ClothingConfig {

    /**
     * Шторка не шире 640 dp, а в ней две колонки: картинка плитки не больше ~250 dp по длинной стороне.
     * 512 px хватает телефону до xxxhdpi и планшету до xhdpi.
     */
    const val CATALOG_MIN_SIDE = 512

    /** В альбомной ориентации с открытой шторкой кукла уменьшается сильнее чем вдвое, и без mipmap мелкие детали рябят. */
    const val DOLL_MIPMAP = true

    /** Каждому кешу картинок 1/16 кучи: хватает на одетую куклу или экран каталога. */
    const val CACHE_HEAP_DIVISOR = 16

    fun dataset(): TemporaryDatasetConfig {
        val cacheBytes = (Runtime.getRuntime().maxMemory() / CACHE_HEAP_DIVISOR).toInt()
        return TemporaryDatasetConfig(
            catalogMinSide = CATALOG_MIN_SIDE,
            dollMipmap = DOLL_MIPMAP,
            catalogCacheBytes = cacheBytes,
            dollCacheBytes = cacheBytes,
        )
    }
}
