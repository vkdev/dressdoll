package ru.vkdev.dressdoll.buildlogic.clothing

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import javax.imageio.ImageIO

@CacheableTask
abstract class GenerateDatasetConfigTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val configFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val dollImage: RegularFileProperty

    /** Только имена: картинки на вход не попадают, и их правка не перезапускает задачу. */
    @get:Input
    abstract val categoryFolders: ListProperty<String>

    @get:Input
    abstract val strayFiles: ListProperty<String>

    /** Папки вещей с именами файлов и размерами `front.png` и `back.png`. */
    @get:Input
    abstract val items: ListProperty<ClothSrcItem>

    /** Все `scale.json` под [clothSrcDir]: относительные пути различают вещи с одинаковым содержимым. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val scaleFiles: ConfigurableFileCollection

    // Читаются только scale.json, а они уже объявлены в scaleFiles.
    @get:Internal
    abstract val clothSrcDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val categories = ClothingDatasetValidator.validate(
            configJson = configFile.get().asFile.readText(),
            categoryFolders = categoryFolders.get().toSet(),
            strayFiles = strayFiles.get().toSet(),
        )
        val dollFile = dollImage.get().asFile
        val image = ImageIO.read(dollFile) ?: throw GradleException("${dollFile.name} не читается как картинка")
        val clothSrc = clothSrcDir.get().asFile
        val doll = DollPlacementValidator.validate(
            canvas = ImageSize(image.width, image.height),
            figure = DollPlacementValidator.figureBounds(image),
            items = items.get(),
            scaleJson = { item -> clothSrc.resolve(item.path).resolve(ItemFiles.SCALE).readText() },
        )

        val categoriesConfig = categories.config
        val dollConfig = doll.config
        if (categoriesConfig == null || dollConfig == null) {
            throw GradleException(
                (categories.errors + doll.errors)
                    .joinToString(separator = "\n", prefix = "Датасет одежды не согласован:\n") { "  - $it" },
            )
        }

        val root = outputDir.get().asFile
        root.deleteRecursively()
        val dir = root.resolve(CategoriesConfigCodegen.PACKAGE.replace('.', '/'))
        dir.mkdirs()
        dir.resolve(CategoriesConfigCodegen.FILE_NAME).writeText(CategoriesConfigCodegen.generate(categoriesConfig))
        dir.resolve(DollConfigCodegen.FILE_NAME).writeText(DollConfigCodegen.generate(dollConfig))
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
