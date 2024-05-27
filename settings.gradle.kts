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
include(":packages:storage-api")
include(":packages:storage-api-drive-gms")
include(":packages:storage-api-drive-nongms")
