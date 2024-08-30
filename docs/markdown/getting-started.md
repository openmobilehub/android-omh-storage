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

## Installation

To integrate a storage provider into your React Native project, follow the specific steps for each provider:

- [Google Drive](https://openmobilehub.github.io/android-omh-storage/docs/plugin-googledrive-gms)
- [OneDrive](https://openmobilehub.github.io/android-omh-storage/docs/plugin-onedrive)
- [Dropbox](https://openmobilehub.github.io/android-omh-storage/docs/plugin-dropbox)

## Usage

### 💡 GOOD TO KNOW

> Any operation you can perform on files can also be applied to folders.

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
```

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

> For more details on file metadata support for each provider, please refer to the [File metadata documentation](https://openmobilehub.github.io/android-omh-storage/docs/#file-metadata).

### Get file versions

Retrieves the versions of a file with the given file ID.

```kotlin
val fileVersions = omhStorageClient.getFileVersions(fileId = "fileId")
```

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

> For more details on file permissions support for each provider, please refer to the [File permissions documentation](https://openmobilehub.github.io/android-omh-storage/docs/#file-permissions).

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

For a more in depth view on the available methods, access the [Reference API](https://openmobilehub.github.io/android-omh-storage/api/packages/core/com.openmobilehub.android.storage.core/-omh-storage-client).

## Sample app

Explore the [sample app](https://github.com/openmobilehub/android-omh-storage/tree/main/apps/storage-sample) included in the repository to see the implementation of storage with Google Drive and other storage providers. The sample app demonstrates the integration and usage of the various storage providers, providing a practical example to help you get started quickly.
