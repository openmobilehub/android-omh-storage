Module plugin-googledrive-gms

<p align="center">
  <a href="https://openmobilehub.github.io/android-omh-storage/docs/">
    <img width="500px" src="https://openmobilehub.org/wp-content/uploads/sites/13/2024/06/OpenMobileHub-horizontal-color.svg"/><br/>
  </a>
  <h2 align="center">Android OMH Storage - Google Drive (GMS)</h2>
</p>

<p align="center">
  <a href="https://central.sonatype.com/artifact/com.openmobilehub.android.storage/plugin-googledrive-gms"><img src="https://img.shields.io/maven-central/v/com.openmobilehub.android.storage/plugin-googledrive-gms" alt="NPM version"/></a>
  <a href="https://github.com/openmobilehub/android-omh-storage/blob/main/LICENSE"><img src="https://img.shields.io/github/license/openmobilehub/android-omh-storage" alt="License"/></a>
</p>

<p align="center">
  <a href="https://discord.com/invite/yTAFKbeVMw"><img src="https://img.shields.io/discord/1115727214827278446.svg?style=flat&colorA=7289da&label=Chat%20on%20Discord" alt="Chat on Discord"/></a>
  <a href="https://twitter.com/openmobilehub"><img src="https://img.shields.io/twitter/follow/rnfirebase.svg?style=flat&colorA=1da1f2&colorB=&label=Follow%20on%20Twitter" alt="Follow on Twitter"/></a>
</p>

---

## Prerequisites

Ensure you have the following packages installed before proceeding with the integration:

- [`com.openmobilehub.android.storage:core:<version>`](https://openmobilehub.github.io/android-omh-storage/docs/core)
- [`com.openmobilehub.android.auth:core:<version>`](https://github.com/openmobilehub/android-omh-auth)

## Installation

To integrate the Google Drive storage provider into your Android project, follow these steps:

### 1. Configure Maven Central repository

Ensure Maven Central is included as a repository in your root **build.gradle** file:

```gradle
allprojects {
  repositories {
    mavenCentral()
  }
}
```

### 2. Add dependency for the Google Drive storage provider

Add the dependency for the Google Drive storage provider to your project's **build.gradle** file:

```gradle
dependencies {
  implementation("com.openmobilehub.android.storage:plugin-googledrive-gms:<version>")
  implementation("com.openmobilehub.android.storage:plugin-googledrive-non-gms:<version>")
}
```

## Configuration

### Console App

To access Google Drive APIs, follow these steps to obtain the **Client ID**:

1. [Create a new app](https://developers.google.com/identity/protocols/oauth2/native-app#android) in [Google Cloud console](https://console.cloud.google.com/projectcreate).
2. Create an OAuth 2.0 Client ID Android application and specify your app's [**Package Name**](https://developer.android.com/build/configure-app-module#set-application-id) and [**SHA1 Fingerprint**](https://support.google.com/cloud/answer/6158849?authuser=1#installedapplications&zippy=%2Cnative-applications%2Candroid).
3. [Enable Google Drive API](https://support.google.com/googleapi/answer/6158841) in [Google Cloud Console](https://console.developers.google.com).

### Secrets

To securely configure the Google Drive storage provider, add the following entry to your project's **local.properties** file:

```bash
GOOGLE_CLIENT_ID=<YOUR_GOOGLE_CLIENT_ID>
```

## Usage

### Initializing

To interact with the Google Drive storage provider, you must first initialize both the OMH Auth Client and OMH Storage Client with the necessary configurations. This setup ensures compatibility with both GMS and non-GMS Android devices.

```kotlin
val omhAuthClient = OmhAuthProvider.Builder()
    .addNonGmsPath("com.openmobilehub.android.auth.plugin.google.nongms.presentation.OmhAuthFactoryImpl")
    .addGmsPath("com.openmobilehub.android.auth.plugin.google.gms.OmhAuthFactoryImpl")
    .build()
    .provideAuthClient(
        context = context,
        scopes = listOf(
            "openid",
            "email",
            "profile",
            "https://www.googleapis.com/auth/drive",
            "https://www.googleapis.com/auth/drive.file"
        ),
        clientId = "<YOUR_GOOGLE_CLIENT_ID>"
    )

val omhStorageClient = OmhStorageProvider.Builder()
    .addGmsPath(GoogleDriveGmsConstants.IMPLEMENTATION_PATH)
    .addNonGmsPath(GoogleDriveNonGmsConstants.IMPLEMENTATION_PATH)
    .build()
    .provideStorageClient(omhAuthClient, context)
```

### Other methods

Interacting with the Google Drive storage provider follows the same pattern as other storage providers since they all implement the [`OmhStorageClient`](https://openmobilehub.github.io/android-omh-storage/api/packages/core/com.openmobilehub.android.storage.core/-omh-storage-client) interface. This uniformity ensures consistent functionality across different storage providers, so you won’t need to learn new methods regardless of the storage provider you choose! For a comprehensive list of available methods, refer to the [Getting Started](https://openmobilehub.github.io/android-omh-storage/docs/getting-started#usage) guide.

#### Caveats

> The methods `downloadFile` and `downloadFileVersion` do not support [Google Workspace documents](https://developers.google.com/drive/api/guides/about-files#types:~:text=Google%20Workspace%20document,MIME%20types.) (Google Docs, Google Sheets, and Google Slides). To download Google Workspace documents, please use the `exportFile` method to export the file to a supported format.

> The method `createPermission` will override `sendNotificationEmail` parameter to `true` when creating permission with `OWNER` role.

#### Escape Hatch

This plugin provides an escape hatch to access the native Google Drive Android SDK. This allows developers to use the underlying provider's API directly, should they need to access a feature of the provider that is not supported by the OMH plugin.

You can obtain the Drive client instances by casting the result of `getProviderSdk` to `Drive`:

```kotlin
import com.google.api.services.drive.Drive
...
omhStorageClient.getProviderSdk() as Drive
```

## License

- See [LICENSE](https://github.com/openmobilehub/android-omh-storage/blob/main/LICENSE)
