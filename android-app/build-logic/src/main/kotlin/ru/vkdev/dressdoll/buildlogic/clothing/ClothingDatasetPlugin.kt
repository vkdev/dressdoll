package ru.vkdev.dressdoll.buildlogic.clothing

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

class ClothingDatasetPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        val assets = layout.projectDirectory.dir("src/main/assets")
        val folders = providers.of(CategoryFoldersSource::class.java) {
            parameters.root.set(assets.dir("cloth-src"))
        }

        val validate = tasks.register<ValidateClothingDatasetTask>("validateClothingDataset") {
            group = "verification"
            description = "Проверяет, что categories.json согласован с папками cloth-src."
            configFile.set(assets.file("categories.json"))
            categoryFolders.set(folders)
            marker.set(layout.buildDirectory.file("validateClothingDataset/ok"))
        }

        // preBuild создаёт Android-плагин, и он может быть применён позже этого.
        tasks.named { it == "preBuild" }.configureEach { dependsOn(validate) }
    }
}
