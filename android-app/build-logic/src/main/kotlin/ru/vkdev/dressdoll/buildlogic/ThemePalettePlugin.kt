package ru.vkdev.dressdoll.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

class ThemePalettePlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply(AndroidLibraryComposePlugin::class.java)

        extensions.configure<LibraryExtension> {
            testOptions {
                unitTests.isIncludeAndroidResources = true
                unitTests.all {
                    // Robolectric needs access to JDK internals on recent JDKs.
                    it.jvmArgs("--add-exports=java.base/jdk.internal.access=ALL-UNNAMED")
                }
            }
        }

        dependencies {
            add("implementation", project(":theme:api"))
            add("implementation", project(":theme:common"))
            add("implementation", libs.library("androidx-compose-ui"))
            add("implementation", libs.library("androidx-compose-material3"))
            add("implementation", platform(libs.library("koin-bom")))
            add("implementation", libs.library("koin-core"))

            add("testImplementation", project(":theme:testing"))
        }
    }
}
