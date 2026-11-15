plugins {
    alias(libs.plugins.dressdoll.android.library.compose)
}

android {
    namespace = "ru.vkdev.dressdoll.theme.testing"
}

dependencies {
    api(project(":theme:api"))
    api(libs.junit)
    api(libs.robolectric)
    api(libs.androidx.test.core)
    api(platform(libs.koin.bom))
    api(libs.koin.core)

    implementation(libs.androidx.compose.ui)
}
