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
}

val useLocalProjects = project.rootProject.extra["useLocalProjects"] as Boolean

dependencies {
    if (useLocalProjects) {
        api(project(":packages:core"))
    } else {
        api("com.openmobilehub.android:storage-api:1.0.5-beta")
    }

    // Retrofit setup
    implementation(Libs.retrofit)
    implementation(Libs.retrofitJacksonConverter)
    implementation(Libs.okHttp)
    implementation(Libs.okHttpLoggingInterceptor)

    // Test dependencies
    testImplementation(Libs.junit)
    testImplementation(Libs.mockk)
}