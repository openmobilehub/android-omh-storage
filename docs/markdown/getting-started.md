---
title: Getting Started
layout: default
nav_order: 2
---

# Getting Started with Android OMH Storage

Android OMH Storage is a project that integrates various cloud storage providers into Android applications. It offers a unified API to work with different authentication providers.

## Prerequisites

Before integrating any OMH Storage provider into your Android project, ensure you meet the following requirements:

- **Gradle version**: 7.0 or higher
- **Android API level**: 23 or higher

Additionally, all providers depend on the [`com.openmobilehub.android.storage:core:2.0.0`](https://miniature-adventure-4gle9ye.pages.github.io/docs/android-omh-storage/core) package. Make sure to install it first before proceeding further!

## Installation

To integrate an OMH Storage provider into your Android project, follow the steps below to install one of the available Maven Central packages:

| Provider                   | Package                                                                                                                                     |
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

### 2. Add Dependency for the desired provider

Add the appropriate dependency for the desired storage provider to your project's **build.gradle** file. Replace <provider-name> with the specific provider package name as shown in the table above:

```gradle
dependencies {
  implementation("com.openmobilehub.android.storage:plugin-<provider-name>:2.0.0")
}
```

## Provider configuration

Each storage provider requires specific secrets for configuration. Please follow the individual provider configuration guides:

- [Core](https://miniature-adventure-4gle9ye.pages.github.io/docs/android-omh-storage/core/#configuration)
- [Google Drive](https://miniature-adventure-4gle9ye.pages.github.io/docs/android-omh-storage/plugin-googledrive-gms/#configuration)
- [OneDrive](https://miniature-adventure-4gle9ye.pages.github.io/docs/android-omh-storage/plugin-onedrive/#configuration)
- [Dropbox](https://miniature-adventure-4gle9ye.pages.github.io/docs/android-omh-storage/plugin-dropbox/#configuration)

## Usage

In this guide, we'll use the Google Drive storage provider as an example. You can choose any other provider since the exposed methods are identical across all providers. Each provider inherits from the [`OmhStorageClient`](https://miniature-adventure-4gle9ye.pages.github.io/api/packages/core/com.openmobilehub.android.storage.core/-omh-storage-client) interface, ensuring consistent functionality. This uniformity means you won't need to learn new methods regardless of the provider you choose!

### Initializing

Before interacting with any provider, it's crucial to initialize the necessary components. This involves setting up the OMH Auth Client and OMH Storage Client with platform-specific configurations tailored to each provider's requirements.

```kotlin

```

For a more in depth view on the available methods, access the [Reference API](https://miniature-adventure-4gle9ye.pages.github.io/api).

## Sample app

Explore the [sample app](https://miniature-adventure-4gle9ye.pages.github.io/docs/android-omh-storage/contributing#sample-app) included in the repository to see the implementation of authentication with Google Drive and other providers. The sample app demonstrates the integration and usage of the various storage providers, providing a practical example to help you get started quickly.
