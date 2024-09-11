plugins {
    `android-base-lib`
}

android {
    namespace = "com.openmobilehub.android.storage.plugin.googledrive.nongms"

    defaultConfig {
        buildConfigField(
            type = "String",
            name = "G_STORAGE_URL",
            value = getRequiredValueFromEnvOrProperties("googleStorageUrl")
        )
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

val useLocalProjects = project.rootProject.extra["useLocalProjects"] as Boolean

dependencies {
    if (useLocalProjects) {
        api(project(":packages:core"))
    } else {
        api("com.openmobilehub.android.storage:core:2.0.5-alpha")
    }

    // Omh Auth
    api(Libs.omhGoogleNonGmsAuthLibrary)

    // Retrofit setup
    implementation(Libs.retrofit)
    implementation(Libs.retrofitJacksonConverter)
    implementation(Libs.okHttp)
    implementation(Libs.okHttpLoggingInterceptor)

    // Test dependencies
    testImplementation(kotlin("test"))
    testImplementation(Libs.junit)
    testImplementation(Libs.mockk)
    testImplementation(Libs.coroutineTesting)
    testImplementation(Libs.json)
}