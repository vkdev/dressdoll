plugins {
    alias(libs.plugins.dressdoll.android.library)
}

android {
    namespace = "ru.vkdev.dressdoll.core.datastore"
}

dependencies {
    // DataStore, CoroutineScope и StateFlow входят в публичный API.
    api(libs.androidx.datastore.core)
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
