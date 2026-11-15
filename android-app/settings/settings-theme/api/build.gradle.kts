plugins {
    alias(libs.plugins.dressdoll.android.library.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "ru.vkdev.dressdoll.settings.theme.api"
}

dependencies {
    api(project(":navigation:api"))
    implementation(libs.kotlinx.serialization.core)
}
