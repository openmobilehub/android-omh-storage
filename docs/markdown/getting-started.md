---
title: Getting Started
layout: default
nav_order: 2
---

# Getting Started with Android OMH Storage

Android OMH Storage is a project that integrates various cloud storage providers into Android applications. It offers a unified API to work with different storage providers.

## Prerequisites

Before integrating any storage provider into your Android project, ensure you meet the following requirements:

- **Gradle version**: 7.0 or higher
- **Android API level**: 23 or higher

Additionally, ensure you have the following packages installed before proceeding with the integration:

- [`com.openmobilehub.android.storage:core:2.0.0`](https://miniature-adventure-4gle9ye.pages.github.io/docs/core)
- [`com.openmobilehub.android.auth:core:2.0.2`](https://github.com/openmobilehub/android-omh-auth)

## Installation

To integrate a storage provider into your Android project, follow the steps below to install one of the available Maven Central packages:

| Storage provider           | Package                                                                                                                                     |
| -------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
| Google Drive (GMS/non-GMS) | `com.openmobilehub.android.storage:plugin-googledrive-gms:2.0.0` <br/> `com.openmobilehub.android.storage:plugin-googledrive-non-gms:2.0.0` |
| OneDrive                   | `com.openmobilehub.android.storage:plugin-onedrive:2.0.0`                                                                                   |
| OneDrive                   | `com.openmobilehub.android.storage:plugin-dropbox:2.0.0`                                                                                    |

### 1. Configure Maven Central repository

Add the following code snippet to your root **build.gradle** file to ensure Maven Central is included as a repository:

```gradle
allprojects {
  repositories {
    mavenCentral()
  }
}
```

### 2. Add Dependency for the desired storage provider

Add the appropriate dependency for the desired storage provider to your project's **build.gradle** file. Replace <storage-provider-name> with the specific storage provider package name as shown in the table above:

```gradle
dependencies {
  implementation("com.openmobilehub.android.storage:plugin-<storage-provider-name>:2.0.0")
}
```

## Storage provider configuration

Each storage provider requires specific secrets for configuration. Please follow the individual storage provider configuration guides:

- [Core](https://miniature-adventure-4gle9ye.pages.github.io/docs/core/#configuration)
- [Google Drive](https://miniature-adventure-4gle9ye.pages.github.io/docs/plugin-googledrive-gms/#configuration)
- [OneDrive](https://miniature-adventure-4gle9ye.pages.github.io/docs/plugin-onedrive/#configuration)
- [Dropbox](https://miniature-adventure-4gle9ye.pages.github.io/docs/plugin-dropbox/#configuration)

## Usage

In this guide, we'll use the Google Drive storage provider as an example. You can choose any other storage provider since the exposed methods are identical across all storage storage providers. Each storage provider implements the [`OmhStorageClient`](https://miniature-adventure-4gle9ye.pages.github.io/api/packages/core/com.openmobilehub.android.storage.core/-omh-storage-client) interface, ensuring consistent functionality. This uniformity means you won't need to learn new methods regardless of the storage provider you choose!

### Initializing

Before interacting with any storage provider, you must first initialize both the OMH Auth Client and OMH Storage Client with the necessary configurations specific to that storage provider.

```kotlin

```

For a more in depth view on the available methods, access the [Reference API](https://miniature-adventure-4gle9ye.pages.github.io/api/packages/core/com.openmobilehub.android.storage.core/-omh-storage-client).

## Sample app

Explore the [sample app](https://miniature-adventure-4gle9ye.pages.github.io/docs/contributing#sample-app) included in the repository to see the implementation of storage with Google Drive and other storage providers. The sample app demonstrates the integration and usage of the various storage providers, providing a practical example to help you get started quickly.
