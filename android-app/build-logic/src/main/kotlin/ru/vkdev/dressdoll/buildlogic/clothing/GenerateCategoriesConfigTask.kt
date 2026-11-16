package ru.vkdev.dressdoll.buildlogic.clothing

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/** Проверяет `categories.json` по папкам cloth-src и генерирует из него Kotlin-конфиг. */
@CacheableTask
abstract class GenerateCategoriesConfigTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val configFile: RegularFileProperty

    /** Только имена: картинки на вход не попадают, и их правка не перезапускает задачу. */
    @get:Input
    abstract val categoryFolders: ListProperty<String>

    @get:Input
    abstract val strayFiles: ListProperty<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val result = ClothingDatasetValidator.validate(
            configJson = configFile.get().asFile.readText(),
            categoryFolders = categoryFolders.get().toSet(),
            strayFiles = strayFiles.get().toSet(),
        )
        val config = result.config ?: throw GradleException(
            result.errors.joinToString(separator = "\n", prefix = "categories.json не согласован с cloth-src:\n") { "  - $it" },
        )

        val root = outputDir.get().asFile
        root.deleteRecursively()
        val file = root.resolve(CategoriesConfigCodegen.PACKAGE.replace('.', '/')).resolve(CategoriesConfigCodegen.FILE_NAME)
        file.parentFile.mkdirs()
        file.writeText(CategoriesConfigCodegen.generate(config))
    }
}

/**
 * Содержимое cloth-src через [ValueSource], а не `provider {}`: с configuration cache
 * значение пересчитывается при каждой сборке, и новая папка не теряется.
 */
abstract class ClothSrcEntriesSource : ValueSource<ClothSrcEntries, ClothSrcEntriesSource.Parameters> {

    interface Parameters : ValueSourceParameters {
        val root: DirectoryProperty
    }

    override fun obtain(): ClothSrcEntries = ClothSrcEntries.scan(parameters.root.get().asFile)
}
