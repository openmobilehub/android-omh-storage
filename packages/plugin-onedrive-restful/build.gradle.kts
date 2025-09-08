plugins {
    `android-base-lib`
}

android {
    namespace = "com.openmobilehub.android.storage.plugin.onedrive.restful"

    defaultConfig {
        buildConfigField(
            type = "String",
            name = "MSGRAPH_API_URL",
            value = getRequiredValueFromEnvOrProperties("msGraphApiUrl"),
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
        implementation(project(":packages:core-restful-common"))
    } else {
        api("com.openmobilehub.android.storage:core:2.1.0-alpha")
        implementation("com.openmobilehub.android.storage:core-restful-common:2.1.0-alpha")
    }

    // Omh Auth
    api(Libs.omhGoogleNonGmsAuthLibrary)

    // slf4j
    implementation(Libs.slf4jApi)

    // Retrofit setup
    implementation(Libs.retrofit)
    implementation(Libs.retrofitJacksonConverter)
    implementation(Libs.okHttp)
    implementation(Libs.okHttpLoggingInterceptor)
    implementation(Libs.jacksonKotlin)

    implementation(Libs.coroutinesCore)
    implementation(Libs.coroutinesAndroid)

    // Test dependencies
    testImplementation(kotlin("test"))
    testImplementation(Libs.junit)
    testImplementation(Libs.mockk)
    testImplementation(Libs.coroutineTesting)
    testImplementation(Libs.json)
    testImplementation(Libs.slf4jAndroid)
}