package ru.vkdev.dressdoll.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

class FeatureImplPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply(AndroidLibraryComposePlugin::class.java)

        tasks.withType<KotlinCompilationTask<*>>().configureEach {
            compilerOptions.optIn.add("org.koin.core.annotation.KoinExperimentalAPI")
        }

        dependencies {
            add("implementation", platform(libs.library("koin-bom")))
            add("implementation", libs.library("koin-core"))
            add("implementation", libs.library("koin-compose"))
            add("implementation", libs.library("koin-compose-navigation3"))
        }
    }
}
