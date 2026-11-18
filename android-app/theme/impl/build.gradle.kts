plugins {
    alias(libs.plugins.dressdoll.android.library.compose)
}

android {
    namespace = "ru.vkdev.dressdoll.theme.impl"
}

dependencies {
    api(project(":theme:api"))
    implementation(project(":core:coroutines"))
    implementation(project(":theme:ui"))

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.core)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(project(":theme:palette:cinnamon"))
    testImplementation(project(":theme:palette:disco"))
    testImplementation(project(":theme:palette:avocado"))
    testImplementation(project(":theme:palette:powder"))
    testImplementation(project(":theme:palette:denim"))
}
