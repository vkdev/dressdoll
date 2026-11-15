package ru.vkdev.dressdoll.temporarydataset.bitmap

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

/** Blocking image decoder. Returns null when the image file does not exist. */
internal fun interface BitmapDecoder {
    fun decode(key: TDImageKey): Bitmap?
}

/** Decodes images from asset paths; [open] throws [FileNotFoundException] for a missing file. */
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
        return stream.use { BitmapFactory.decodeStream(it, null, OPTIONS) }
            ?: throw IOException("Cannot decode ${key.assetPath}")
    }

    private companion object {
        // Clothing images need transparency, so no RGB_565.
        val OPTIONS = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
    }
}
