plugins {
    alias(libs.plugins.dressdoll.android.library.compose)
}

android {
    namespace = "ru.vkdev.dressdoll.disco.api"
}

dependencies {
    // Modifier и Rect входят в публичный API.
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui)
}
