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
        val stream = try {
            open(key.assetPath)
        } catch (_: FileNotFoundException) {
            return null
        }
        // Options изменяемые (декодер пишет в поля out*), поэтому у каждого параллельного декодирования свои.
        // Одежде нужна прозрачность, поэтому не RGB_565.
        val options = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
        val bitmap = stream.use { BitmapFactory.decodeStream(it, null, options) }
            ?: throw IOException("Cannot decode ${key.assetPath}")
        // В альбомной ориентации с открытой шторкой кукла уменьшается сильнее чем вдвое,
        // и без mipmap мелкие детали рябят. Mipmap строится при загрузке в GPU.
        if (key.onDoll) bitmap.setHasMipMap(true)
        return bitmap
    }
}
