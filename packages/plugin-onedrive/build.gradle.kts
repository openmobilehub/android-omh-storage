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
        api("com.openmobilehub.android.storage:core:2.1.0-alpha")
    }

    // MsGraph
    api(Libs.msGraph)

    // slf4j
    implementation(Libs.slf4jApi)

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
    testImplementation(Libs.slf4jAndroid)
}