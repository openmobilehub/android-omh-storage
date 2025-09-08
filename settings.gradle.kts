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
include(":packages:core-restful-common")
include(":packages:plugin-googledrive-gms")
include(":packages:plugin-googledrive-non-gms")
include(":packages:plugin-onedrive")
include(":packages:plugin-onedrive-restful")
include(":packages:plugin-dropbox")
include(":packages:plugin-dropbox-restful")