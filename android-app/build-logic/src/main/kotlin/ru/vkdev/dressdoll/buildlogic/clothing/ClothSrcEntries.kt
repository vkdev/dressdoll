package ru.vkdev.dressdoll.buildlogic.clothing

import java.io.DataInputStream
import java.io.File
import java.io.IOException
import java.io.Serializable

/**
 * Содержимое cloth-src, каким его увидит `AssetManager.list()` в приложении.
 * [Serializable] — чтобы значение [ClothSrcEntriesSource] попало в configuration cache.
 *
 * [folders] и [files] — содержимое корня, [items] — папки вещей внутри папок категорий.
 */
data class ClothSrcEntries(
    val folders: List<String>,
    val files: List<String>,
    val items: List<ClothSrcItem>,
) : Serializable {

    companion object {

        fun scan(root: File): ClothSrcEntries {
            val packaged = packagedChildren(root)
            val categories = packaged.filter { it.isDirectory }
            return ClothSrcEntries(
                folders = categories.map { it.name },
                files = packaged.filterNot { it.isDirectory }.map { it.name },
                items = categories.flatMap { category ->
                    packagedChildren(category).map { entry ->
                        if (entry.isDirectory) scanItem(category.name, entry) else ClothSrcItem.strayFile(category.name, entry.name)
                    }
                },
            )
        }

        private fun scanItem(category: String, folder: File): ClothSrcItem {
            val files = packagedChildren(folder).filterNot { it.isDirectory }
            // Картинки на вход задачи не попадают, только их размеры: правка пикселей не перезапускает проверку.
            val sizes = files
                .filter { it.name in SIZED_IMAGES }
                .associate { it.name to readPngSize(it) }
            return ClothSrcItem(category, folder.name, isFolder = true, files = files.map { it.name }, imageSizes = sizes)
        }

        private fun packagedChildren(dir: File): List<File> =
            dir.listFiles().orEmpty().filter(::isPackaged).sortedBy { it.name }

        // Повторяет ignoreAssetsPattern AGP по умолчанию
        // (`!.svn:!.git:!.ds_store:!*.scc:.*:<dir>_*:!CVS:!thumbs.db:!picasa.ini:!*~`):
        // то, что AAPT не упакует, в приложении не появится, и сверять это с конфигом нельзя.
        private fun isPackaged(file: File): Boolean {
            val name = file.name.lowercase()
            return !(
                name.startsWith(".") ||
                    name.endsWith("~") ||
                    name.endsWith(".scc") ||
                    name in setOf("cvs", "thumbs.db", "picasa.ini") ||
                    (file.isDirectory && name.startsWith("_"))
                )
        }

        private val SIZED_IMAGES = setOf(ItemFiles.FRONT, ItemFiles.BACK)
    }
}

/**
 * Запись в папке категории. [isFolder] `false` — файл, а не папка вещи: в приложении
 * `AssetManager.list()` выдал бы его за вещь.
 *
 * [imageSizes] — размеры `front.png` и `back.png`; `null` у файла, который не читается как PNG.
 */
data class ClothSrcItem(
    val category: String,
    val folder: String,
    val isFolder: Boolean,
    val files: List<String>,
    val imageSizes: Map<String, ImageSize?>,
) : Serializable {

    val path: String get() = "$category/$folder"

    companion object {
        fun strayFile(category: String, name: String) =
            ClothSrcItem(category, name, isFolder = false, files = emptyList(), imageSizes = emptyMap())
    }
}

data class ImageSize(val width: Int, val height: Int) : Serializable {
    override fun toString(): String = "${width}×$height"
}

internal object ItemFiles {
    const val SCALE = "scale.json"
    const val FRONT = "front.png"
    const val BACK = "back.png"
    const val CATALOG = "catalog.png"
}

private val PNG_SIGNATURE = byteArrayOf(-119, 'P'.code.toByte(), 'N'.code.toByte(), 'G'.code.toByte(), 13, 10, 26, 10)

/** Размер из заголовка IHDR, без декодирования картинки; `null`, если это не PNG. */
internal fun readPngSize(file: File): ImageSize? = try {
    DataInputStream(file.inputStream().buffered()).use { input ->
        val signature = ByteArray(PNG_SIGNATURE.size).also(input::readFully)
        input.readInt() // длина чанка IHDR
        val chunk = ByteArray(4).also(input::readFully)
        if (!signature.contentEquals(PNG_SIGNATURE) || String(chunk, Charsets.US_ASCII) != "IHDR") {
            null
        } else {
            ImageSize(width = input.readInt(), height = input.readInt())
        }
    }
} catch (_: IOException) {
    null
}
