package ru.vkdev.dressdoll.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.pluginId("android-library"))

        extensions.configure<LibraryExtension> {
            compileSdk = libs.intVersion("compileSdk")
            defaultConfig {
                minSdk = libs.intVersion("minSdk")
            }
            testOptions {
                unitTests.all {
                    // Robolectric на свежих JDK нужен доступ к внутренним пакетам JDK.
                    it.jvmArgs("--add-exports=java.base/jdk.internal.access=ALL-UNNAMED")
                }
            }
        }
    }
}
