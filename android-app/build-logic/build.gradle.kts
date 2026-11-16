plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin.api)
    compileOnly(libs.compose.gradlePlugin)
    implementation(libs.jackson.core)

    testImplementation(libs.junit)
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
        register("themePalette") {
            id = libs.plugins.dressdoll.theme.palette.get().pluginId
            implementationClass = "ru.vkdev.dressdoll.buildlogic.ThemePalettePlugin"
        }
        register("clothingDataset") {
            id = libs.plugins.dressdoll.clothing.dataset.get().pluginId
            implementationClass = "ru.vkdev.dressdoll.buildlogic.clothing.ClothingDatasetPlugin"
        }
    }
}
