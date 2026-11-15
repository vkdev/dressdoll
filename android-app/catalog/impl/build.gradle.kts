plugins {
    alias(libs.plugins.dressdoll.feature.impl)
}

android {
    namespace = "ru.vkdev.dressdoll.catalog.impl"
}

dependencies {
    api(project(":catalog:api"))
    implementation(project(":theme:api"))

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
}
