package ru.vkdev.dressdoll.temporarydataset.bitmap

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

/** Блокирующий. Возвращает `null`, если файла нет. */
internal fun interface BitmapDecoder {
    fun decode(key: TDImageKey): Bitmap?
}

/** [open] бросает [FileNotFoundException], если файла нет. */
internal class AssetBitmapDecoder(
    private val open: (assetPath: String) -> InputStream,
) : BitmapDecoder {

    constructor(assets: AssetManager) : this(assets::open)

    override fun decode(key: TDImageKey): Bitmap? {
        // Options изменяемые (декодер пишет в поля out*), поэтому у каждого параллельного декодирования свои.
        // Одежде нужна прозрачность, поэтому не RGB_565.
        val options = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
        // Картинки куклы не уменьшаются: их позиции на холсте заданы в пикселях исходника.
        if (!key.onDoll) {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            (openOrNull(key) ?: return null).use { BitmapFactory.decodeStream(it, null, bounds) }
            options.inSampleSize = catalogSampleSize(bounds.outWidth, bounds.outHeight)
        }
        val stream = openOrNull(key) ?: return null
        val bitmap = stream.use { BitmapFactory.decodeStream(it, null, options) }
            ?: throw IOException("Cannot decode ${key.assetPath}")
        // В альбомной ориентации с открытой шторкой кукла уменьшается сильнее чем вдвое,
        // и без mipmap мелкие детали рябят. Mipmap строится при загрузке в GPU.
        if (key.onDoll) bitmap.setHasMipMap(true)
        return bitmap
    }

    private fun openOrNull(key: TDImageKey): InputStream? = try {
        open(key.assetPath)
    } catch (_: FileNotFoundException) {
        null
    }
}

internal fun catalogSampleSize(width: Int, height: Int): Int {
    val side = maxOf(width, height)
    var sample = 1
    while (side / (sample * 2) >= CATALOG_MIN_SIDE) sample *= 2
    return sample
}

// Шторка не шире 640 dp, а в ней две колонки: картинка плитки не больше ~250 dp по длинной стороне.
// 512 px хватает телефону до xxxhdpi и планшету до xhdpi.
internal const val CATALOG_MIN_SIDE = 512
