plugins {
    alias(libs.plugins.dressdoll.android.library.compose)
}

android {
    namespace = "ru.vkdev.dressdoll.theme.api"
}

dependencies {
    // ColorScheme, Typography и StateFlow входят в публичный API.
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.material3)
    api(libs.kotlinx.coroutines.core)
}
