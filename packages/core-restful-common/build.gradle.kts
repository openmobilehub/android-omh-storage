plugins {
    `android-base-lib`
}

android {
    namespace = "com.openmobilehub.android.storage.core.restful.common"

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

val useLocalProjects = project.rootProject.extra["useLocalProjects"] as Boolean

dependencies {
    if (useLocalProjects) {
        api(project(":packages:core"))
    } else {
        api("com.openmobilehub.android.storage:core:2.1.0-alpha")
    }

    // slf4j
    implementation(Libs.slf4jApi)

    // Retrofit setup
    implementation(Libs.retrofit)
    implementation(Libs.retrofitJacksonConverter)
    implementation(Libs.okHttp)
    implementation(Libs.okHttpLoggingInterceptor)
    implementation(Libs.jacksonKotlin)

    // Test dependencies
    testImplementation(kotlin("test"))
    testImplementation(Libs.junit)
    testImplementation(Libs.mockk)
    testImplementation(Libs.coroutineTesting)
    testImplementation(Libs.json)
    testImplementation(Libs.slf4jAndroid)
}