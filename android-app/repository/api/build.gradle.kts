plugins {
    alias(libs.plugins.dressdoll.android.library)
}

android {
    namespace = "ru.vkdev.dressdoll.repository.api"
}

dependencies {
    // Flow входит в публичный API.
    api(libs.kotlinx.coroutines.core)
}
