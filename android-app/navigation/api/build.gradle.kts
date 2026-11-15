plugins {
    alias(libs.plugins.dressdoll.android.library.compose)
}

android {
    namespace = "ru.vkdev.dressdoll.navigation.api"
}

dependencies {
    api(libs.androidx.navigation3.runtime)
}
