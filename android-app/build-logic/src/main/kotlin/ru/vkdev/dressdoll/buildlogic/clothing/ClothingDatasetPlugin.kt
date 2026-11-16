package ru.vkdev.dressdoll.buildlogic.clothing

import com.android.build.api.variant.LibraryAndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

class ClothingDatasetPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        val entries = providers.of(ClothSrcEntriesSource::class.java) {
            parameters.root.set(layout.projectDirectory.dir("src/main/assets/cloth-src"))
        }

        val generate = tasks.register<GenerateCategoriesConfigTask>("generateCategoriesConfig") {
            group = "build"
            description = "Проверяет categories.json по папкам cloth-src и генерирует из него Kotlin-конфиг."
            configFile.set(layout.projectDirectory.file("categories.json"))
            categoryFolders.set(entries.map { it.folders })
            strayFiles.set(entries.map { it.files })
        }

        // Сгенерированный код — источник компиляции, поэтому проверка конфига идёт перед любой сборкой модуля.
        pluginManager.withPlugin("com.android.library") {
            extensions.getByType<LibraryAndroidComponentsExtension>().onVariants { variant ->
                variant.sources.kotlin?.addGeneratedSourceDirectory(generate, GenerateCategoriesConfigTask::outputDir)
            }
        }
    }
}
