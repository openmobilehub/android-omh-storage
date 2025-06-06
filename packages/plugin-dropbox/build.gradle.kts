plugins {
    `android-base-lib`
}

android {
    namespace = "com.openmobilehub.android.storage.plugin.dropbox"
}

val useLocalProjects = project.rootProject.extra["useLocalProjects"] as Boolean

dependencies {
    if (useLocalProjects) {
        api(project(":packages:core"))
    } else {
        api("com.openmobilehub.android.storage:core:2.1.0-alpha")
    }

    // Dropbox
    api(Libs.dropboxCore)

    // Annotation
    implementation(Libs.androidxAnnotation)
    implementation(Libs.coroutinesCore)

    // slf4j
    implementation(Libs.slf4jApi)

    // Test dependencies
    testImplementation(kotlin("test"))
    testImplementation(Libs.junit)
    testImplementation(Libs.mockk)
    testImplementation(Libs.coroutineTesting)
    testImplementation(Libs.slf4jAndroid)
}