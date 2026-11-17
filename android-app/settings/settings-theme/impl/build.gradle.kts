plugins {
    alias(libs.plugins.dressdoll.feature.impl)
}

android {
    namespace = "ru.vkdev.dressdoll.settings.theme.impl"
}

dependencies {
    api(project(":settings:settings-theme:api"))
    implementation(project(":theme:api"))
    implementation(project(":disco:api"))

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.runtime.compose)
}
