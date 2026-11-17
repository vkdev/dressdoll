plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "ru.vkdev.dressdoll"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "ru.vkdev.dressdoll"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":theme:impl"))
    implementation(project(":theme:palette:cinnamon"))
    implementation(project(":theme:palette:disco"))
    implementation(project(":theme:palette:avocado"))
    implementation(project(":theme:palette:powder"))
    implementation(project(":theme:palette:denim"))
    implementation(project(":doll:impl"))
    implementation(project(":catalog:impl"))
    implementation(project(":outfit:impl"))
    implementation(project(":repository:impl"))
    implementation(project(":navigation:impl"))
    implementation(project(":settings:settings-theme:impl"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
}
