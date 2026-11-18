plugins {
    alias(libs.plugins.dressdoll.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "ru.vkdev.dressdoll.doll.api"
}

dependencies {
    api(project(":navigation:api"))
    implementation(libs.kotlinx.serialization.core)
}
