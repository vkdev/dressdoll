plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "ru.vkdev.dressdoll.doll.api"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    api(project(":navigation:api"))
    implementation(libs.kotlinx.serialization.core)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
}
