plugins {
    alias(libs.plugins.dressdoll.android.library)
}

android {
    namespace = "ru.vkdev.dressdoll.clothing.api"
}

dependencies {
    // Flow входит в публичный API.
    api(libs.kotlinx.coroutines.core)
}
