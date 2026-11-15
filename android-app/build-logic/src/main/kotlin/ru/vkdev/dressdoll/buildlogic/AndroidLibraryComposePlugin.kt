package ru.vkdev.dressdoll.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/** Android library + Jetpack Compose. Namespace задаёт сам модуль. */
class AndroidLibraryComposePlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.pluginId("android-library"))
        pluginManager.apply(libs.pluginId("kotlin-compose"))

        extensions.configure<LibraryExtension> {
            compileSdk = libs.intVersion("compileSdk")
            defaultConfig {
                minSdk = libs.intVersion("minSdk")
            }
            buildFeatures {
                compose = true
            }
        }

        dependencies {
            add("implementation", platform(libs.library("androidx-compose-bom")))
        }
    }
}
