Module plugin-dropbox

<p align="center">
  <a href="https://openmobilehub.github.io/android-omh-storage/docs/">
    <img width="500px" src="https://openmobilehub.org/wp-content/uploads/sites/13/2024/06/OpenMobileHub-horizontal-color.svg"/><br/>
  </a>
  <h2 align="center">Android OMH Storage - Dropbox</h2>
</p>

<p align="center">
  <a href="https://central.sonatype.com/artifact/com.openmobilehub.android.storage/plugin-dropbox"><img src="https://img.shields.io/maven-central/v/com.openmobilehub.android.storage/plugin-dropbox" alt="NPM version"/></a>
  <a href="https://github.com/openmobilehub/android-omh-storage/blob/main/LICENSE"><img src="https://img.shields.io/github/license/openmobilehub/android-omh-storage" alt="License"/></a>
</p>

<p align="center">
  <a href="https://discord.com/invite/yTAFKbeVMw"><img src="https://img.shields.io/discord/1115727214827278446.svg?style=flat&colorA=7289da&label=Chat%20on%20Discord" alt="Chat on Discord"/></a>
  <a href="https://twitter.com/openmobilehub"><img src="https://img.shields.io/twitter/follow/rnfirebase.svg?style=flat&colorA=1da1f2&colorB=&label=Follow%20on%20Twitter" alt="Follow on Twitter"/></a>
</p>

---

## Prerequisites

Ensure you have the following packages installed before proceeding with the integration:

- [`com.openmobilehub.android.storage:core:2.0.0`](https://openmobilehub.github.io/android-omh-storage/docs/core)
- [`com.openmobilehub.android.auth:core:2.0.2`](https://github.com/openmobilehub/android-omh-auth)

## Installation

To integrate the Dropbox storage provider into your Android project, follow these steps:

### 1. Configure Maven Central repository

Ensure Maven Central is included as a repository in your root **build.gradle** file:

```gradle
allprojects {
  repositories {
    mavenCentral()
  }
}
```

### 2. Add dependency for the Dropbox Drive storage provider

Add the dependency for the Dropbox storage provider to your project's **build.gradle** file:

```gradle
dependencies {
  implementation("com.openmobilehub.android.storage:plugin-dropbox-gms:2.0.0")
}
```

## Configuration

### Console App

To access Dropbox APIs, follow these steps to obtain the **Client ID**:

1. [Create a new app](https://developers.dropbox.com/oauth-guide) in [Dropbox Console](https://www.dropbox.com/developers/apps/create).
2. Enable the `account_info.read`, `files.metadata.read`, `files.content.write`, `files.content.read`, `sharing.write` and `sharing.read` permission for your app.

### Secrets

To securely configure the Dropbox storage provider, add the following entry to your project's **local.properties** file:

```bash
DROPBOX_CLIENT_ID=<YOUR_DROPBOX_APP_KEY>
```

## Usage

### Initializing

To interact with the Dropbox storage provider, you must first initialize both the OMH Auth Client and OMH Storage Client with the necessary configurations.

```kotlin
val omhAuthClient = DropboxAuthClient(
    scopes = arrayListOf("account_info.read", "files.metadata.read", "files.content.write", "files.content.read", "sharing.write", "sharing.read"),
    context = context,
    appId = BuildConfig.DROPBOX_CLIENT_ID,
)

val omhStorageClient = DropboxOmhStorageFactory().getStorageClient(omhAuthClient)
```

### Other methods

Interacting with the Dropbox storage provider follows the same pattern as other storage providers since they all implement the [`OmhStorageClient`](https://openmobilehub.github.io/android-omh-storage/api/packages/core/com.openmobilehub.android.storage.core/-omh-storage-client) interface. This uniformity ensures consistent functionality across different storage providers, so you won’t need to learn new methods regardless of the storage provider you choose! For a comprehensive list of available methods, refer to the [Getting Started](https://openmobilehub.github.io/android-omh-storage/docs/getting-started) guide.

#### Caveats

> When updating a file, if the new file has a different name than the updated file, two additional versions might sometimes appear in the system. One version comes from updating the content of the file, and the other comes from updating the file name. However, this behavior is non-deterministic, and sometimes only one new version is added. This is why it is listed under the Caveats section.

## License

- See [LICENSE](https://github.com/openmobilehub/android-omh-storage/blob/main/LICENSE)
