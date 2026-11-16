package ru.vkdev.dressdoll.buildlogic.clothing

import java.io.File
import java.io.Serializable

/**
 * Содержимое cloth-src, каким его увидит `AssetManager.list()` в приложении.
 * [Serializable] — чтобы значение [ClothSrcEntriesSource] попало в configuration cache.
 */
data class ClothSrcEntries(val folders: List<String>, val files: List<String>) : Serializable {

    companion object {

        fun scan(root: File): ClothSrcEntries {
            val packaged = root.listFiles().orEmpty().filter(::isPackaged).sortedBy { it.name }
            return ClothSrcEntries(
                folders = packaged.filter { it.isDirectory }.map { it.name },
                files = packaged.filterNot { it.isDirectory }.map { it.name },
            )
        }

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
    }
}
