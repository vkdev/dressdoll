plugins {
    alias(libs.plugins.dressdoll.android.library.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "ru.vkdev.dressdoll.settings.api"
}

dependencies {
    api(project(":navigation:api"))
    // LazyListScope входит в публичный API.
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.foundation)
    implementation(libs.kotlinx.serialization.core)
}
