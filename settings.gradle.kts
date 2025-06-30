pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}


dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google() // ✅ WAJIB DI SINI SAJA
        mavenCentral()
    }
}

rootProject.name = "test"
include(":app")
