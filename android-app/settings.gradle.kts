pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "DressDoll"

include(":app")
include(":theme:api")
include(":theme:impl")
include(":doll:api")
include(":doll:impl")
include(":repository:api")
include(":repository:impl")
