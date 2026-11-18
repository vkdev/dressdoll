package ru.vkdev.dressdoll.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryComposePlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply(AndroidLibraryPlugin::class.java)
        pluginManager.apply(libs.pluginId("kotlin-compose"))

        extensions.configure<LibraryExtension> {
            buildFeatures {
                compose = true
            }
        }

        dependencies {
            add("implementation", platform(libs.library("androidx-compose-bom")))
        }
    }
}
