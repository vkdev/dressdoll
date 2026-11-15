plugins {
    alias(libs.plugins.dressdoll.android.library.compose)
}

android {
    namespace = "ru.vkdev.dressdoll.catalog.api"
}

dependencies {
    // ClothingItemId is part of the public API.
    api(project(":repository:api"))
    implementation(libs.androidx.compose.ui)
}
