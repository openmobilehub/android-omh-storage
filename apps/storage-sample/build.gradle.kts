import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    `android-application`
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android") version "2.44" apply true
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

@Suppress("UnstableApiUsage")
android {
    namespace = "com.openmobilehub.android.storage.sample"

    defaultConfig {
        applicationId = "com.openmobilehub.android.storage.sample"
        buildConfigField(
            type = "String",
            name = "AUTH_NON_GMS_PATH",
            value =  "\"com.omh.android.auth.nongms.presentation.OmhAuthFactoryImpl\""
        )
        buildConfigField(
            type = "String",
            name = "AUTH_GMS_PATH",
            value = "\"com.omh.android.auth.gms.OmhAuthFactoryImpl\""
        )
        buildConfigField(
            type = "String",
            name = "STORAGE_GMS_PATH",
            value = "\"com.openmobilehub.android.storage.plugin.google.gms.OmhGmsStorageFactoryImpl\""
        )
        buildConfigField(
            type = "String",
            name = "STORAGE_NON_GMS_PATH",
            value = "\"com.openmobilehub.android.storage.plugin.google.nongms.OmhNonGmsStorageFactoryImpl\""
        )
    }

    signingConfigs {
        create("release") {
            val localProperties = gradleLocalProperties(rootDir)
            storeFile = file(localProperties["keypath"].toString())
            storePassword = localProperties["keypass"].toString()
            keyAlias = localProperties["keyalias"].toString()
            keyPassword = localProperties["keypassword"].toString()
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    viewBinding {
        enable = true
    }

    kapt {
        correctErrorTypes = true
    }
}

val useLocalProjects = project.rootProject.extra["useLocalProjects"] as Boolean

dependencies {
    implementation(Libs.coreKtx)
    implementation(Libs.lifecycleKtx)
    implementation(Libs.viewModelKtx)
    implementation(Libs.activityKtx)
    implementation(Libs.fragmentKtx)
    implementation(Libs.androidAppCompat)
    implementation(Libs.material)
    implementation(Libs.coroutinesAndroid)
    implementation(Libs.splash)
    implementation(Libs.navigationFragmentKtx)
    implementation(Libs.navigationUIKtx)
    implementation("com.github.bumptech.glide:glide:4.14.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    annotationProcessor("com.github.bumptech.glide:compiler:4.14.2")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.44")
    kapt("com.google.dagger:hilt-compiler:2.44")

    testImplementation(Libs.junit)

    implementation("com.openmobilehub.android:auth-api-gms:1.0.1-beta")
    implementation("com.openmobilehub.android:auth-api-non-gms:1.0.1-beta")

    // Use local implementation instead of dependencies
    if (useLocalProjects) {
        implementation(project(":packages:core"))
        implementation(project(":packages:plugin-google-gms"))
        implementation(project(":packages:plugin-google-non-gms"))
    } else {
        implementation("com.openmobilehub.android:storage-api-drive-nongms:1.0.8-beta")
        implementation("com.openmobilehub.android:storage-api-drive-gms:1.0.7-beta")
    }
}