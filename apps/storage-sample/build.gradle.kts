import java.net.URLEncoder

plugins {
    `android-application`
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android") version Versions.hilt apply true
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("org.jetbrains.dokka") version Versions.dokka
}

@Suppress("UnstableApiUsage")
android {
    namespace = "com.openmobilehub.android.storage.sample"

    defaultConfig {
        applicationId = "com.openmobilehub.android.storage.sample"
        versionCode = 1
        versionName = "1.0"
        minSdk = 26

        val dropboxAppKey = getValueFromEnvOrProperties("DROPBOX_APP_KEY")
        val microsoftClientId = getValueFromEnvOrProperties("MICROSOFT_CLIENT_ID")
        val microsoftSignatureHash = getValueFromEnvOrProperties("MICROSOFT_SIGNATURE_HASH")

        resValue("string", "db_login_protocol_scheme", "db-${dropboxAppKey}")

        resValue("string", "microsoft_path", "/${microsoftSignatureHash}")

        val rawDir = file("./src/main/res/raw")
        if (!rawDir.exists()) {
            rawDir.mkdirs()
        }
        file("./src/main/res/raw/ms_auth_config.json").writeText(
            """
{
  "client_id": "$microsoftClientId",
  "authorization_user_agent": "DEFAULT",
  "redirect_uri": "msauth://com.openmobilehub.android.storage.sample.AndroidApplication/${
                URLEncoder.encode(
                    microsoftSignatureHash,
                    "UTF-8"
                )
            }",
  "authorities": [
    {
      "type": "AAD",
      "audience": {
        "type": "PersonalMicrosoftAccount",
        "tenant_id": "consumers"
      }
    }
  ],
  "account_mode": "SINGLE"
}
            """.trimIndent()
        )

    }

    signingConfigs {
        // It creates a signing config for release builds if the required properties are set.
        // The if statement is necessary to avoid errors when the packages are built on CI.
        // The alternative would be to pass all the environment variables for signing apk to the packages workflows.
        create("release") {
            val storeFileName =
                getValueFromEnvOrProperties("SAMPLE_APP_KEYSTORE_FILE_NAME", null)
            val storePassword =
                getValueFromEnvOrProperties("SAMPLE_APP_KEYSTORE_STORE_PASSWORD", null)
            val keyAlias =
                getValueFromEnvOrProperties("SAMPLE_APP_KEYSTORE_KEY_ALIAS", null)
            val keyPassword =
                getValueFromEnvOrProperties("SAMPLE_APP_KEYSTORE_KEY_PASSWORD", null)

            @Suppress("ComplexCondition")
            if (storeFileName != null && storePassword != null && keyAlias != null && keyPassword != null) {
                this.storeFile = file(storeFileName)
                this.storePassword = storePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // If the signing config is set, it will be used for release builds.
            if (signingConfigs["release"].storeFile != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
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
    implementation(Libs.glide)
    implementation(Libs.constraintlayout)
    implementation(Libs.dataStore)
    implementation(Libs.hiltAndroid)
    kapt(Libs.hiltCompiler)
    kapt(Libs.glideCompiler)

    // Fixes crash on Android 23 while saving file to downloads
    implementation(Libs.guava)

    // Auth
    implementation(Libs.omhGoogleNonGmsAuthLibrary)
    implementation(Libs.omhGoogleGmsAuthLibrary)
    implementation(Libs.omhDropboxAuthLibrary)
    implementation(Libs.omhMicrosoftAuthLibrary)

    implementation(Libs.dokka)

    // Use local implementation instead of dependencies
    if (useLocalProjects) {
        implementation(project(":packages:core"))
        implementation(project(":packages:plugin-googledrive-gms"))
        implementation(project(":packages:plugin-googledrive-non-gms"))
        implementation(project(":packages:plugin-onedrive"))
        implementation(project(":packages:plugin-dropbox"))
    } else {
        implementation("com.openmobilehub.android:storage-api-drive-nongms:1.0.8-beta")
        implementation("com.openmobilehub.android:storage-api-drive-gms:1.0.7-beta")
        implementation("com.openmobilehub.android:storage-api-onedrive:1.0.0-beta")
        implementation("com.openmobilehub.android:storage-api-dropbox:1.0.0-beta")
    }

    testImplementation(Libs.junit)
}

tasks.dokkaHtmlPartial {
    enabled = false
}