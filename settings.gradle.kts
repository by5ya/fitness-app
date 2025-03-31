pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // или PREFER_PROJECT
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "fitness-app"
include(":app")
 