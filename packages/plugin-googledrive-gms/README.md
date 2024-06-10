<p align="center">
  <a href="https://www.openmobilehub.com/">
    <img width="160px" src="https://www.openmobilehub.com/images/logo/omh_logo.png"/><br/>
  </a>
  <h2 align="center">Android OMH Storage - Google Drive (GMS/non-GMS)</h2>
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

Ensure you have the [`com.openmobilehub.android.storage:core:2.0.0`](https://www.openmobilehub.com/android-omh-storage/core) package installed before proceeding with the integration.

## Installation

To integrate the Google Drive OMH Storage provider into your Android project, follow these steps:

### 1. Configure Maven Central repository

Ensure Maven Central is included as a repository in your root **build.gradle** file:

```gradle
allprojects {
  repositories {
    mavenCentral()
  }
}
```

### 2. Add Dependency for the Google Drive provider

Add the dependency for the Google Drive provider to your project's **build.gradle** file:

```gradle
dependencies {
  implementation("com.openmobilehub.android.storage:plugin-googledrive-gms:2.0.0")
  implementation("com.openmobilehub.android.storage:plugin-googledrive-non-gms:2.0.0")
}
```

## Configuration

### Console App

To access Google Drive APIs, follow these steps to obtain the **Client ID**:

1. [Create a new app](https://developers.google.com/identity/protocols/oauth2/native-app#android) in [Google Cloud console](https://console.cloud.google.com/projectcreate).
2. Create an OAuth 2.0 Client ID Android application and specify your app's [**Package Name**](https://developer.android.com/build/configure-app-module#set-application-id) and [**SHA1 Fingerprint**](https://support.google.com/cloud/answer/6158849?authuser=1#installedapplications&zippy=%2Cnative-applications%2Candroid).
3. [Enable Google Drive API](https://support.google.com/googleapi/answer/6158841) in [Google Cloud Console](https://console.developers.google.com).

### Secrets

To securely configure the Google Drive provider, add the following entry to your project's **local.properties** file:

```bash
GOOGLE_CLIENT_ID=<YOUR_GOOGLE_CLIENT_ID>
```

## Usage

### Initializing

<!-- TODO: Document the initialization -->

Before interacting with Google Drive, initialize the OMH Auth Client and OMH Storage Client with the necessary platform-specific configuration:

```kotlin

```

### Other methods

Interacting with the Google Drive provider follows the same pattern as other providers since they all implement the [`OmhStorageClient`]() interface. For a comprehensive list of available methods, refer to the [Quick Start](https://www.openmobilehub.com/react-native-omh-auth/docs/getting-started#sign-in) guide.

## License

- See [LICENSE](https://github.com/openmobilehub/android-omh-storage/blob/main/LICENSE)
