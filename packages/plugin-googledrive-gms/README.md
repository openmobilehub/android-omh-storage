<p align="center">
  <a href="https://www.openmobilehub.com/">
    <img width="160px" src="https://www.openmobilehub.com/images/logo/omh_logo.png"/><br/>
  </a>
  <h2 align="center">Android OMH Storage</h2>
</p>

<p align="center">
  <a href="https://central.sonatype.com/artifact/com.openmobilehub.android.storage/plugin-googledrive-gms"><img src="https://img.shields.io/maven-central/v/com.openmobilehub.android.storage/plugin-googledrive-gms" alt="NPM version"/></a>
  <a href="https://github.com/openmobilehub/android-omh-storage/blob/main/LICENSE"><img src="https://img.shields.io/github/license/openmobilehub/android-omh-auth" alt="License"/></a>
</p>

<p align="center">
  <a href="https://discord.com/invite/yTAFKbeVMw"><img src="https://img.shields.io/discord/1115727214827278446.svg?style=flat&colorA=7289da&label=Chat%20on%20Discord" alt="Chat on Discord"/></a>
  <a href="https://twitter.com/openmobilehub"><img src="https://img.shields.io/twitter/follow/rnfirebase.svg?style=flat&colorA=1da1f2&colorB=&label=Follow%20on%20Twitter" alt="Follow on Twitter"/></a>
</p>

---

# Module plugin-googledrive-gms

## Set up your Google Cloud project for applications with Google Services (Google Auth)

To access Google APIs, generate a unique client_id for your app in the Google API Console. Add the
client_id to your app's code and complete the required Cloud Console setup steps:

### Steps

1. [Go to the Google Cloud Console and open the project selector page](https://console.cloud.google.com/projectselector2).
2. Click on "Create Project" to start creating a new Cloud project.
3. [Go to the Credentials page](https://console.cloud.google.com/apis/credentials).
4. On the Credentials page, click on "Create credentials" and choose "OAuth Client ID".
5. In the "Application Type" option, select "Android".
6. Set your application package name (Use "com.openmobilehub.android.auth.sample" if you are
   following the starter-code)
7. Update the debug/release SHA-1 certificate fingerprint for Android's Client ID.
   Note: The debug build is automatically signed with the debug keystore. Obtain the certificate
   fingerprint from it by following the guidelines in the official Google Developers
   documentation: ["Using keytool on the certificate"](https://developers.google.com/android/guides/client-auth#using_keytool_on_the_certificate).
8. In the [OAuth consent screen](https://console.cloud.google.com/apis/credentials/consent) add the
   test users that you will be using for QA and development. Without this step you won't be able to
   access the application while it's in testing mode.
9. You're all set!

## Add the Client ID to your app

You should not check your Client ID into your version control system, so it is recommended
storing it in the `local.properties` file, which is located in the root directory of your project.
For more information about the `local.properties` file,
see [Gradle properties](https://developer.android.com/studio/build#properties-files) [files](https://developer.android.com/studio/build#properties-files).

1. Open the `local.properties` in your project level directory, and then add the following code.
   Replace `YOUR_GOOGLE_CLIENT_ID` with your API key. `GOOGLE_CLIENT_ID=YOUR_GOOGLE_CLIENT_ID`
2. Save the
   file and [sync your project with Gradle](https://developer.android.com/studio/build#sync-files).

## Gradle configuration

To incorporate the Google GMS and non-GMS plugin into your project, you have two options: directly
include the Android OMH Client libraries dependencies or utilize the Android OMH Core Plugin.

### Directly including the Google GMS and non-GMS dependencies

If you want to incorporate Google plugin into your project without using the Android OMH Core
plugin, you have to directly include the Google GMS and non-GMS plugins as a dependency. In
the `build.gradle.kts`, add the following implementation statement to the `dependencies{}` section:

```groovy
implementation("com.openmobilehub.android.auth:plugin-google-gms:2.0.0")
implementation("com.openmobilehub.android.auth:plugin-google-non-gms:2.0.0")
```

Save the file
and [sync your project with Gradle](https://developer.android.com/studio/build#sync-files).

### Using the Android OMH Core plugin

Please see the advanced documentation on how to use
the [Android OMH Core](https://www.openmobilehub.com/android-omh-auth/advanced-docs/core/docs/advanced/Plugins.md) Plugin.

## Provide the Google OMH Auth Client

In the `SingletonModule.kt` file in the `:auth-starter-sample` module add the following function to
provide the Google OMH Auth Client:

```kotlin
@Provides
fun providesGoogleAuthClient(@ApplicationContext context: Context): OmhAuthClient {
    val omhAuthProvider = OmhAuthProvider.Builder()
        .addNonGmsPath("com.openmobilehub.android.auth.plugin.google.nongms.presentation.OmhAuthFactoryImpl")
        .addGmsPath("com.openmobilehub.android.auth.plugin.google.gms.OmhAuthFactoryImpl")
        .build()

    return omhAuthProvider.provideAuthClient(
        scopes = listOf("openid", "email", "profile"),
        clientId = BuildConfig.GOOGLE_CLIENT_ID,
        context = context
    )
}
```

> We'd recommend to store the client as a singleton with your preferred dependency injection library
> as this will be your only gateway to the OMH Auth SDK and it doesn't change in runtime at all.
