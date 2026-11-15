plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "ru.vkdev.dressdoll.repository.impl"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
}

dependencies {
    api(project(":repository:api"))
    implementation(project(":temporary-dataset"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)

    testImplementation(libs.junit)
}
