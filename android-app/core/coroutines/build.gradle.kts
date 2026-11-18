plugins {
    alias(libs.plugins.dressdoll.android.library)
}

android {
    namespace = "ru.vkdev.dressdoll.core.coroutines"
}

dependencies {
    // CoroutineScope и Koin Scope входят в публичный API.
    api(libs.kotlinx.coroutines.core)
    api(platform(libs.koin.bom))
    api(libs.koin.core)
}
