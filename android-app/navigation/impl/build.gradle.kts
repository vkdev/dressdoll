plugins {
    alias(libs.plugins.dressdoll.feature.impl)
}

android {
    namespace = "ru.vkdev.dressdoll.navigation.impl"
}

dependencies {
    api(project(":navigation:api"))

    implementation(libs.androidx.navigation3.ui)

    testImplementation(libs.junit)
}
