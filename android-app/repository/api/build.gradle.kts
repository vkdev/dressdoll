plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "ru.vkdev.dressdoll.repository.api"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
}

dependencies {
    // Flow входит в публичный API.
    api(libs.kotlinx.coroutines.core)
}
