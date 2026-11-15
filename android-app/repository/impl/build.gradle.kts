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

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
}
