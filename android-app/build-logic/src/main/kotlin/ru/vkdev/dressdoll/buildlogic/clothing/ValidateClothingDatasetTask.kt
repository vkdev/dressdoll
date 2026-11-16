package ru.vkdev.dressdoll.buildlogic.clothing

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class ValidateClothingDatasetTask : DefaultTask() {

    @get:InputFile
    abstract val configFile: RegularFileProperty

    /** Только имена папок: картинки на вход не попадают, и их правка не перезапускает проверку. */
    @get:Input
    abstract val categoryFolders: ListProperty<String>

    /** Пустой маркер: без выхода Gradle не считает задачу up-to-date. */
    @get:OutputFile
    abstract val marker: RegularFileProperty

    @TaskAction
    fun validate() {
        val errors = ClothingDatasetValidator.validate(
            configJson = configFile.get().asFile.readText(),
            categoryFolders = categoryFolders.get().toSet(),
        )
        if (errors.isNotEmpty()) {
            throw GradleException(
                errors.joinToString(separator = "\n", prefix = "categories.json не согласован с cloth-src:\n") { "  - $it" },
            )
        }
        marker.get().asFile.writeText("")
    }
}

/**
 * Список папок через [ValueSource], а не `provider {}`: с configuration cache
 * значение пересчитывается при каждой сборке, и новая папка не теряется.
 */
abstract class CategoryFoldersSource : ValueSource<List<String>, CategoryFoldersSource.Parameters> {

    interface Parameters : ValueSourceParameters {
        val root: DirectoryProperty
    }

    override fun obtain(): List<String> =
        parameters.root.get().asFile.listFiles()
            .orEmpty()
            .filter { it.isDirectory && !it.name.startsWith(".") }
            .map { it.name }
            .sorted()
}
