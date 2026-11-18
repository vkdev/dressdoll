plugins {
    alias(libs.plugins.dressdoll.android.library)
}

android {
    namespace = "ru.vkdev.dressdoll.clothing.impl"
}

dependencies {
    api(project(":clothing:api"))
    implementation(project(":core:coroutines"))
    implementation(project(":temporary-dataset"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)

    testImplementation(libs.junit)
}
