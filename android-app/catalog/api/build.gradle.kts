plugins {
    alias(libs.plugins.dressdoll.android.library.compose)
}

android {
    namespace = "ru.vkdev.dressdoll.catalog.api"
}

dependencies {
    // ClothingItemId входит в публичный API.
    api(project(":clothing:api"))
    implementation(libs.androidx.compose.ui)
}
