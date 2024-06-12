plugins {
    `android-base-lib`
}

android {
    namespace = "com.openmobilehub.android.storage.plugin.onedrive"
}

val useLocalProjects = project.rootProject.extra["useLocalProjects"] as Boolean

dependencies {
    if (useLocalProjects) {
        api(project(":packages:core"))
    } else {
        api("com.openmobilehub.android:storage-api:1.0.5-beta")
    }

    // MsGraph dependencies
    implementation(Libs.msGraph)

    // Test dependencies
    testImplementation(kotlin("test"))
    testImplementation(Libs.junit)
    testImplementation(Libs.mockk)
    testImplementation(Libs.coroutineTesting)
}