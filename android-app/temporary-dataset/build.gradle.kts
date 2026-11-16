plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.dressdoll.clothing.dataset)
}

// Временный модуль: сырой датасет одежды в ассетах (src/main/assets).
android {
    namespace = "ru.vkdev.dressdoll.temporarydataset"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all {
            // Robolectric на свежих JDK нужен доступ к внутренним пакетам JDK.
            it.jvmArgs("--add-exports=java.base/jdk.internal.access=ALL-UNNAMED")
        }
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.kotlinx.coroutines.test)
}
