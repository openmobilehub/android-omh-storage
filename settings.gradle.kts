pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "omh-storage"

include(":apps:storage-sample")
include(":packages:core")
include(":packages:plugin-google-gms")
include(":packages:plugin-google-non-gms")
