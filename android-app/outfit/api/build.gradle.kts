plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "ru.vkdev.dressdoll.outfit.api"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
}

dependencies {
    // ClothingItemId и StateFlow входят в публичный API.
    api(project(":repository:api"))
    api(libs.kotlinx.coroutines.core)
}
