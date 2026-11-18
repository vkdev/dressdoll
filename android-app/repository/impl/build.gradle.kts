plugins {
    alias(libs.plugins.dressdoll.android.library)
}

android {
    namespace = "ru.vkdev.dressdoll.repository.impl"
}

dependencies {
    api(project(":repository:api"))
    implementation(project(":temporary-dataset"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)

    testImplementation(libs.junit)
}
