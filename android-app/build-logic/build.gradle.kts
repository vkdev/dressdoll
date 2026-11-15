plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin.api)
    compileOnly(libs.compose.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidLibraryCompose") {
            id = libs.plugins.dressdoll.android.library.compose.get().pluginId
            implementationClass = "ru.vkdev.dressdoll.buildlogic.AndroidLibraryComposePlugin"
        }
        register("featureImpl") {
            id = libs.plugins.dressdoll.feature.impl.get().pluginId
            implementationClass = "ru.vkdev.dressdoll.buildlogic.FeatureImplPlugin"
        }
    }
}
