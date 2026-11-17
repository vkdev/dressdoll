package ru.vkdev.dressdoll.buildlogic.clothing

import com.android.build.api.variant.LibraryAndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

class ClothingDatasetPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        val clothSrc = layout.projectDirectory.dir("src/main/assets/cloth-src")
        val entries = providers.of(ClothSrcEntriesSource::class.java) {
            parameters.root.set(clothSrc)
        }

        val generate = tasks.register<GenerateDatasetConfigTask>("generateDatasetConfig") {
            group = "build"
            description = "Проверяет categories.json, doll_1.png и scale.json вещей по папкам cloth-src " +
                "и генерирует из них Kotlin-конфиг."
            configFile.set(layout.projectDirectory.file("categories.json"))
            dollImage.set(layout.projectDirectory.file("src/main/assets/doll_1.png"))
            categoryFolders.set(entries.map { it.folders })
            strayFiles.set(entries.map { it.files })
            items.set(entries.map { it.items })
            clothSrcDir.set(clothSrc)
            scaleFiles.from(fileTree(clothSrc) { include("*/*/${ItemFiles.SCALE}") })
        }

        pluginManager.withPlugin("com.android.library") {
            extensions.getByType<LibraryAndroidComponentsExtension>().onVariants { variant ->
                variant.sources.kotlin?.addGeneratedSourceDirectory(generate, GenerateDatasetConfigTask::outputDir)
            }
        }
    }
}
