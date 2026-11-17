plugins {
    alias(libs.plugins.dressdoll.feature.impl)
}

android {
    namespace = "ru.vkdev.dressdoll.settings.impl"
}

dependencies {
    api(project(":settings:api"))
    implementation(project(":theme:api"))

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.runtime.compose)
}
