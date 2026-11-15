plugins {
    alias(libs.plugins.dressdoll.android.library.compose)
}

android {
    namespace = "ru.vkdev.dressdoll.theme.common"
}

dependencies {
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.material3)

    testImplementation(libs.junit)
}
