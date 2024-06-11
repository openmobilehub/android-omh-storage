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
        api("com.openmobilehub.android:storage-api:1.0.5-beta")
    }

    // Dropbox
    implementation(Libs.dropboxCore)
    implementation("androidx.annotation:annotation-jvm:1.8.0")

    // Test dependencies
    testImplementation(kotlin("test"))
    testImplementation(Libs.junit)
    testImplementation(Libs.mockk)
    testImplementation(Libs.coroutineTesting)
}