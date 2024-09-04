Module plugin-onedrive

<p align="center">
  <a href="https://openmobilehub.github.io/android-omh-storage/docs/">
    <img width="500px" src="https://openmobilehub.org/wp-content/uploads/sites/13/2024/06/OpenMobileHub-horizontal-color.svg"/><br/>
  </a>
  <h2 align="center">Android OMH Storage - OneDrive</h2>
</p>

<p align="center">
  <a href="https://central.sonatype.com/artifact/com.openmobilehub.android.storage/plugin-onedrive"><img src="https://img.shields.io/maven-central/v/com.openmobilehub.android.storage/plugin-onedrive" alt="NPM version"/></a>
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

To integrate the OneDrive storage provider into your Android project, follow these steps:

### 1. Configure Maven Central repository

Ensure Maven Central is included as a repository in your root **build.gradle** file:

```gradle
allprojects {
  repositories {
    mavenCentral()
  }
}
```

### 2. Add dependency for the OneDrive storage provider

Add the dependency for the OneDrive storage provider to your project's **build.gradle** file:

```gradle
dependencies {
  implementation("com.openmobilehub.android.storage:plugin-onedrive-gms:<version>")
}
```

## Configuration

### Console App

To access Microsoft APIs, follow these steps to obtain the **MSAL Configuration**:

1. [Create a new app](https://learn.microsoft.com/en-us/entra/identity-platform/tutorial-v2-android#register-your-application-with-microsoft-entra-id) in [Microsoft Azure](https://portal.azure.com/#view/Microsoft_AAD_RegisteredApps/CreateApplicationBlade).
2. Add the **Android** platform and specify your [**Package Name**](https://developer.android.com/build/configure-app-module#set-application-id) and [**Signature Hash**](https://learn.microsoft.com/en-us/entra/identity-platform/tutorial-v2-android#register-your-application-with-microsoft-entra-id:~:text=In%20the%20Signature%20hash%20section%20of%20the%20Configure%20your%20Android%20app%20pane%2C%20select%20Generating%20a%20development%20Signature%20Hash.%20and%20copy%20the%20KeyTool%20command%20to%20your%20command%20line.) for your app.
3. Copy the **MSAL Configuration** into a newly created JSON file named **ms_auth_config.json** and place it in the **src/main/res/raw** directory.
4. In the **ms_auth_config.json** file, add a new key `"account_mode"` with the value `"SINGLE"`.

### Secrets

To securely configure the OneDrive storage provider, add the following entry to your project's **local.properties** file:

```bash
MICROSOFT_SIGNATURE_HASH=<YOUR_MICROSOFT_SIGNATURE_HASH>
```

## Usage

### Initializing

To interact with the OneDrive storage provider, you must first initialize both the OMH Auth Client and OMH Storage Client with the necessary configurations.

```kotlin
val omhAuthClient = MicrosoftAuthClient(
    configFileResourceId = R.raw.ms_auth_config,
    context = context,
    scopes = arrayListOf("User.Read", "Files.ReadWrite.All"),
)

val omhStorageClient = OneDriveOmhStorageFactory().getStorageClient(omhAuthClient)
```

### Other methods

Interacting with the OneDrive storage provider follows the same pattern as other storage providers since they all implement the [`OmhStorageClient`](https://openmobilehub.github.io/android-omh-storage/api/packages/core/com.openmobilehub.android.storage.core/-omh-storage-client) interface. This uniformity ensures consistent functionality across different storage providers, so you won’t need to learn new methods regardless of the storage provider you choose! For a comprehensive list of available methods, refer to the [Getting Started](https://openmobilehub.github.io/android-omh-storage/docs/getting-started#usage) guide.

#### Caveats

> When updating a file, if the new file has a different name than the updated file, two additional versions might sometimes appear in the system. One version comes from updating the content of the file, and the other comes from updating the file name. However, this behavior is non-deterministic, and sometimes only one new version is added. This is why it is listed under the Caveats section.

> The [Sharing Links](https://learn.microsoft.com/en-us/graph/api/resources/permission?view=graph-rest-1.0#sharing-links) permissions are not supported.

> When creating permission for a user with a given email address, there has to be a Microsoft Account assigned to this email. Otherwise, an exception will be thrown with the user message: "Some users in the request cannot be invited securely". This is caused by `requireSignIn` flag being set to true in the invitation request. The library does that as the [Sharing Links](https://learn.microsoft.com/en-us/graph/api/resources/permission?view=graph-rest-1.0#sharing-links) are not yet supported, and for some accounts, when inviting users without a Microsoft Account, a sharing link will be created instead of permission. 

> The [search](https://learn.microsoft.com/en-us/graph/api/driveitem-search?view=graph-rest-1.0&tabs=java) query takes several fields including filename, metadata, and file content when searching.

#### Escape Hatch

This plugin provides an escape hatch to access the native OneDrive Android SDK. This allows developers to use the underlying provider's API directly, should they need to access a feature of the provider that is not supported by the OMH plugin.

You can obtain the OneDrive client instances by casting the result of `getProviderSdk` to `GraphServiceClient`:

```kotlin
import com.microsoft.graph.serviceclient.GraphServiceClient
...
omhStorageClient.getProviderSdk() as GraphServiceClient
```

## License

- See [LICENSE](https://github.com/openmobilehub/android-omh-storage/blob/main/LICENSE)
