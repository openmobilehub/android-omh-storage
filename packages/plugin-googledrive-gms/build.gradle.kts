plugins {
    `android-base-lib`
}

@Suppress("UnstableApiUsage")
android {
    namespace = "com.openmobilehub.android.storage.plugin.googledrive.gms"

    lint {
        disable.add("DuplicatePlatformClasses")
    }

    packagingOptions {
        resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        resources.excludes.add("/META-INF/DEPENDENCIES")
        resources.excludes.add("/META-INF/LICENSE")
        resources.excludes.add("/META-INF/LICENSE.txt")
        resources.excludes.add("/META-INF/license.txt")
        resources.excludes.add("/META-INF/NOTICE")
        resources.excludes.add("/META-INF/NOTICE.txt")
        resources.excludes.add("/META-INF/notice.txt")
        resources.excludes.add("/META-INF/ASL2.0")
    }
}

val useLocalProjects = project.rootProject.extra["useLocalProjects"] as Boolean

dependencies {
    if (useLocalProjects) {
        api(project(":packages:core"))
    } else {
        api("com.openmobilehub.android.storage:core:2.0.1-alpha")
    }

    // Omh Auth
    api(Libs.omhGoogleGmsAuthLibrary)

    // GMS
    implementation(Libs.googlePlayServicesAuth)
    implementation(Libs.googleJacksonClient)
    implementation(Libs.googleAndroidApiClient) {
        exclude("org.apache.httpcomponents")
    }
    api(Libs.googleDrive) {
        exclude("org.apache.httpcomponents")
    }
    implementation(Libs.avoidGuavaConflict)
    implementation(Libs.httpClientGson)

    // Test dependencies
    testImplementation(kotlin("test"))
    testImplementation(Libs.junit)
    testImplementation(Libs.mockk)
    testImplementation(Libs.coroutineTesting)
}