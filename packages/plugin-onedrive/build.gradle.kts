plugins {
    `android-base-lib`
}

android {
    namespace = "com.openmobilehub.android.storage.plugin.onedrive"
    defaultConfig {
        minSdk = 26
        buildConfigField(
            type = "String",
            name = "ONEDRIVE_API_URL",
            value = getRequiredValueFromEnvOrProperties("oneDriveStorageUrl")
        )
    }
}

val useLocalProjects = project.rootProject.extra["useLocalProjects"] as Boolean

dependencies {
    if (useLocalProjects) {
        api(project(":packages:core"))
    } else {
        api("com.openmobilehub.android.storage:core:2.0.2-alpha")
    }

    // MsGraph
    api(Libs.msGraph)

    // Retrofit setup
    implementation(Libs.retrofit)
    implementation(Libs.retrofitJacksonConverter)
    implementation(Libs.okHttp)
    implementation(Libs.okHttpLoggingInterceptor)

    // Annotation
    implementation(Libs.androidxAnnotation)

    // Test dependencies
    testImplementation(kotlin("test"))
    testImplementation(Libs.junit)
    testImplementation(Libs.mockk)
    testImplementation(Libs.coroutineTesting)
}