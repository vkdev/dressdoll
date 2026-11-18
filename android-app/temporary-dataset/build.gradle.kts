plugins {
    alias(libs.plugins.dressdoll.android.library)
    alias(libs.plugins.dressdoll.clothing.dataset)
}

// Временный модуль: сырой датасет одежды в ассетах (src/main/assets).
android {
    namespace = "ru.vkdev.dressdoll.temporarydataset"
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.kotlinx.coroutines.test)
}
