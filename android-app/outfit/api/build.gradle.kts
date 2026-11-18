plugins {
    alias(libs.plugins.dressdoll.android.library)
}

android {
    namespace = "ru.vkdev.dressdoll.outfit.api"
}

dependencies {
    // ClothingItemId и StateFlow входят в публичный API.
    api(project(":repository:api"))
    api(libs.kotlinx.coroutines.core)
}
