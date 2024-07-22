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
- **Android API level**: 23(GoogleDrive, Dropbox), 26(OneDrive) or higher

Additionally, ensure you have the following packages installed before proceeding with the integration:

- [`com.openmobilehub.android.storage:core:2.0.0`](https://miniature-adventure-4gle9ye.pages.github.io/docs/core)
- [`com.openmobilehub.android.auth:core:2.0.2`](https://github.com/openmobilehub/android-omh-auth)

## Installation

To integrate a storage provider into your Android project, follow the steps below to install one of the available Maven Central packages:

| Storage provider       | Package                                                              | Min Android API level |
| ---------------------- | -------------------------------------------------------------------- | --------------------- |
| Google Drive (GMS)     | `com.openmobilehub.android.storage:plugin-googledrive-gms:2.0.0`     | 23                    |
| Google Drive (non-GMS) | `com.openmobilehub.android.storage:plugin-googledrive-non-gms:2.0.0` | 23                    |
| OneDrive               | `com.openmobilehub.android.storage:plugin-dropbox:2.0.0`             | 26                    |
| Dropbox                | `com.openmobilehub.android.storage:plugin-dropbox:2.0.0`             | 23                    |

### 1. Configure Maven Central repository

Add the following code snippet to your root **build.gradle** file to ensure Maven Central is included as a repository:

```gradle
allprojects {
  repositories {
    mavenCentral()
  }
}
```

### 2. Add dependency for the desired storage provider

Add the appropriate dependency for the desired storage provider to your project's **build.gradle** file. Replace `<storage-provider-name>` with the specific storage provider package name and `<version>` with the latest version available as shown in the table above:

```gradle
dependencies {
  implementation("com.openmobilehub.android.storage:plugin-<storage-provider-name>:<version>")
}
```

## Storage provider configuration

Each storage provider requires specific secrets for configuration. Please follow the individual storage provider configuration guides:

- [Google Drive](https://miniature-adventure-4gle9ye.pages.github.io/docs/plugin-googledrive-gms/#configuration)
- [OneDrive](https://miniature-adventure-4gle9ye.pages.github.io/docs/plugin-onedrive/#configuration)
- [Dropbox](https://miniature-adventure-4gle9ye.pages.github.io/docs/plugin-dropbox/#configuration)

## Usage

In this guide, we'll use the Google Drive storage provider as an example. You can choose any other storage provider since the exposed methods are identical across all storage storage providers. Each storage provider implements the [`OmhStorageClient`](https://miniature-adventure-4gle9ye.pages.github.io/api/packages/core/com.openmobilehub.android.storage.core/-omh-storage-client) interface, ensuring consistent functionality. This uniformity means you won't need to learn new methods regardless of the storage provider you choose!

### 💡 GOOD TO KNOW

> Any operation you can perform on files can also be applied to folders.

### Initializing

Before interacting with any storage provider, you must first initialize both the OMH Auth Client and OMH Storage Client with the necessary configurations specific to that storage provider.

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

### Get root folder path

Retrieves the root folder path of the storage service. Useful when you want to list files in the root folder.

```kotlin
val rootPath = omhStorageClient.rootFolder
```

### List files

Lists files from a specific folder.

```kotlin
val files = omhStorageClient.listFiles(parentId = "folderId")
```

### Search files

Lists files with names containing the specified query value.

```kotlin
val searchResults = omhStorageClient.search(query = "fileName")
```

### Create folder

Creates a folder in a specific folder.

```kotlin
val newFile = omhStorageClient.createFolder(
    name = "fileName",
    parentId = "folderId"
)
```

### Create file (with mime type)

Creates a file in a specific folder.

```kotlin
val newFile = omhStorageClient.createFileWithMimeType(
    name = "fileName",
    mimeType = "fileMimeType",
    parentId = "folderId"
)
```

### Create file (with extension)

Creates a file in a specific folder.

```kotlin
val newFile = omhStorageClient.createFileWithExtension(
    name = "fileName",
    extension = "ext",
    parentId = "folderId"
)

### Update file

Updates a remote file with the content of a local file.

```kotlin
val updatedFile = omhStorageClient.updateFile(
    localFileToUpload = File("localFilePath"),
    fileId = "fileId"
)
```

### Delete file

Moves a file with the given file ID in the trash.

```kotlin
omhStorageClient.deleteFile(id = "fileId")
```

### Permanently delete file

Permanently deletes a file with the given file ID.

```kotlin
omhStorageClient.permanentlyDeleteFile(id = "fileId")
```

### Upload file

Uploads a local file to a specific folder.

```kotlin
val uploadedFile = omhStorageClient.uploadFile(
    localFileToUpload = File("localFilePath"),
    parentId = "folderId"
)
```

### Download file

Downloads a file with the given file ID.

```kotlin
val fileContent = omhStorageClient.downloadFile(fileId = "fileId")
```

### Export file

Exports a provider application file with the given file ID to a specified MIME type.

```kotlin
val exportedFileContent = omhStorageClient.exportFile(
    fileId = "fileId",
    exportedMimeType = "desiredMimeType"
)
```

### Get file metadata

Retrieves the metadata of a file with the given file ID.

```kotlin
val fileMetadata = omhStorageClient.getFileMetadata(fileId = "fileId")
```

> For more details on file metadata support for each provider, please refer to the [File metadata documentation](https://miniature-adventure-4gle9ye.pages.github.io/docs/#file-metadata).

### Get file versions

Retrieves the versions of a file with the given file ID.

```kotlin
val fileVersions = omhStorageClient.getFileVersions(fileId = "fileId")
```

> For more details on file versioning support for each provider, please refer to the [File versioning documentation](https://miniature-adventure-4gle9ye.pages.github.io/docs/#file-versioning).

### Download file version

Downloads a specific version of a file.

```kotlin
val versionContent = omhStorageClient.downloadFileVersion(
    fileId = "fileId",
    versionId = "versionId"
)
```

### Get file permissions

Lists the permissions of a file with the given file ID.

```kotlin
val permissions = omhStorageClient.getFilePermissions(fileId = "fileId")
```

### Create file permission

Creates a permission for a file.

```kotlin
val userPermission = OmhCreatePermission.CreateIdentityPermission(
    OmhPermissionRole.WRITER,
    OmhPermissionRecipient.User("test@email.com")
)

val newPermission = omhStorageClient.createPermission(
    fileId = "fileId",
    permission = userPermission,
    sendNotificationEmail = true,
    emailMessage = "Optional message"
)
```

> For more details on file permissions support for each provider, please refer to the [File permissions documentation](https://miniature-adventure-4gle9ye.pages.github.io/docs/#file-permissions).

### Update file permission

Updates the role of a permission in a file.

```kotlin
val updatedPermission = omhStorageClient.updatePermission(
    fileId = "fileId",
    permissionId = "permissionId",
    role = OmhPermissionRole.ROLE
)
```

### Delete file permission

Deletes a permission with the given permission ID from a file.

```kotlin
omhStorageClient.deletePermission(
    fileId = "fileId",
    permissionId = "permissionId"
)
```

### Get file URL

Retrieves the file URL.

```kotlin
val webUrl = omhStorageClient.getWebUrl(fileId = "fileId")
```

---

For a more in depth view on the available methods, access the [Reference API](https://miniature-adventure-4gle9ye.pages.github.io/api/packages/core/com.openmobilehub.android.storage.core/-omh-storage-client).

## Sample app

Explore the [sample app](https://github.com/openmobilehub/android-omh-storage/tree/main/apps/storage-sample) included in the repository to see the implementation of storage with Google Drive and other storage providers. The sample app demonstrates the integration and usage of the various storage providers, providing a practical example to help you get started quickly.
