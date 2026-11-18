package ru.vkdev.dressdoll.temporarydataset

/**
 * Значения задаёт потребитель: они зависят от его вёрстки и бюджета памяти.
 *
 * [catalogMinSide] — в пикселях: картинка каталога уменьшается вдвое, пока её длинная сторона не меньше этого.
 */
data class TemporaryDatasetConfig(
    val catalogMinSide: Int,
    val dollMipmap: Boolean,
    val catalogCacheBytes: Int,
    val dollCacheBytes: Int,
)
