plugins {
    alias(libs.plugins.dressdoll.feature.impl)
}

android {
    namespace = "ru.vkdev.dressdoll.doll.impl"
}

dependencies {
    api(project(":doll:api"))
    implementation(project(":core:coroutines"))
    implementation(project(":clothing:api"))
    implementation(project(":outfit:api"))
    implementation(project(":catalog:api"))
    implementation(project(":disco:api"))
    implementation(project(":theme:ui"))
    implementation(project(":settings:api"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
}
