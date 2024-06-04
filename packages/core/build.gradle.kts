plugins {
    `android-base-lib`
    id("org.jetbrains.dokka") version Versions.dokka
}

android {
    namespace = "com.openmobilehub.android.storage.core"
}

dependencies {
    implementation(Libs.reflection)

    // Omh Auth
    api(Libs.omhCoreAuthLibrary)

    // Coroutines
    implementation(Libs.coroutinesCore)
    implementation(Libs.coroutinesAndroid)

    // Play services
    implementation(Libs.googlePlayBase)

    // Test
    testImplementation(Libs.junit)
    androidTestImplementation(Libs.androidJunit)
    testImplementation(Libs.mockk)
    testImplementation(Libs.coroutineTesting)
}
