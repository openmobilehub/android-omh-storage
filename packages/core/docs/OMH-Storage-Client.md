---
title: OMH Storage Client
layout: default
parent: Core
---

The OMH Storage Client acts as the facade of the library serving as the only interface that you'll ever interact with. It provides you with a host of functionalities like get a list of files, create files, download files, update files, upload files and delete files.

# Obtaining the client

To obtain the client in whatever configuration you're running your application you need to use the `OmhStorageProvider`. Depending on if you're using the OMH Core plugin or not, there are two ways of going about it:

## With the core plugin

In case you're using our OMH Core plugin, then configuring the provider is very straightforward, just paste the following code into your project:

```kotlin
val omhAuthProvider: OmhAuthProvider = OmhAuthProvider.Builder()
    .addNonGmsPath(BuildConfig.AUTH_NON_GMS_PATH)
    .addGmsPath(BuildConfig.AUTH_GMS_PATH)
    .build()

val omhAuthClient: OmhAuthClient = omhAuthProvider.provideAuthClient(
    scopes = listOf("openid", "email", "profile"),
    clientId = BuildConfig.CLIENT_ID,
    context = context
)

val omhStorageProvider = OmhStorageProvider.Builder()
            .addNonGmsPath(BuildConfig.STORAGE_GMS_PATH)
            .addGmsPath(BuildConfig.STORAGE_NON_GMS_PATH)
            .build()
            .provideStorageClient(omhAuthClient, context)
```

The BuildConfig fields will be generated automatically once you finish setting up the core plugin.

## Without the core plugin

1. Without the core plugin, we would recommend creating distinct flavors for your GMS and non GMS versions. If you are using a custom implementation of the OMH Storage SDK, save the reflection path of the `OmhStorageFactory` in a variable.
2. Create sourceSets for your classes that will provide the `OmhStorageProvider`, be it static methods or dependency injection modules.
3. For each source set, configure the `Builder` to represent the configuration expected in which the application will run. For example, the configuration for the GMS version would look like this:

```kotlin
val omhAuthProvider: OmhAuthProvider = OmhAuthProvider.Builder()
    .addNonGmsPath(BuildConfig.AUTH_NON_GMS_PATH)
    .addGmsPath(BuildConfig.AUTH_GMS_PATH)
    .build()

val omhAuthClient: OmhAuthClient = omhAuthProvider.provideAuthClient(
    scopes = listOf("openid", "email", "profile"),
    clientId = BuildConfig.CLIENT_ID,
    context = context
)

val omhStorageProvider = OmhStorageProvider.Builder()
            .addGmsPath(BuildConfig.STORAGE_NON_GMS_PATH)
            .build()
            .provideStorageClient(omhAuthClient, context)
```

Once you have the provider setup, you can obtain the Storage client with the following function:

```kotlin
val omhAuthProvider: OmhAuthProvider = OmhAuthProvider.Builder()
    .addNonGmsPath(BuildConfig.AUTH_NON_GMS_PATH)
    .addGmsPath(BuildConfig.AUTH_GMS_PATH)
    .build()

val omhAuthClient: OmhAuthClient = omhAuthProvider.provideAuthClient(
    scopes = listOf("openid", "email", "profile"),
    clientId = BuildConfig.CLIENT_ID,
    context = context
)

val omhStorageProvider: OmhStorageProvider = OmhStorageProvider.Builder()
    .addNonGmsPath(BuildConfig.STORAGE_NON_GMS_PATH)
    .addGmsPath(BuildConfig.STORAGE_GMS_PATH)
    .build()
val omhStorageClient: OmhStorageClient = omhStorageProvider.provideStorageClient(
    authClient = omhAuthClient,
    context = context
)
```

We'd recommend using the client as a singleton instance as once instantiated, the configuration won't change in runtime.

If you are not using the core plugin the you can always pass the path manually to the provider like this:

```kotlin
val omhStorageProvider = OmhStorageProvider.Builder()
            .addGmsPath(com.example.app.factories.MyOwnFactoryImplementation)
            .build()
```

Just don't forget to add your custom implementation as a dependency to the project.

# Using the client

As explained in the [Getting Started Guide](https://github.com/openmobilehub/omh-storage/blob/main/README.md), once you have a reference to the client just call the function you need.

## List files

For list files, just use the instance you created of the `omhStorageClient` and call method `listFiles` sending as parameter the desired parent id.

```kotlin
val cancellable = omhStorageClient.listFiles(parentId)
            .addOnSuccess { result: GetFilesListUseCaseResult ->
                // Get the files list
                val filesList: List<OmhStorageEntity> = result.files
                // TODO - Developer: Manage success
            }
            .addOnFailure { exception: Exception ->
                // TODO - Developer: Manage error
            }
            .execute()
cancellableCollector.addCancellable(cancellable)
```

## Create files

For create files, just use the instance you created of the `omhStorageClient` and call method `createFile` sending as parameter the desired name, mime type and parent id.

```kotlin
 val cancellable = omhStorageClient.createFile(name, mimeType, parentId)
             .addOnSuccess { result: CreateFileUseCaseResult ->
                // An instance of OmhStorageEntity with the information of the created file. In case the file was not created, will be null
                val file: OmhStorageEntity? = result.file
                 // TODO - Developer: Manage success
             }
             .addOnFailure { exception: Exception ->
                 // TODO - Developer: Manage error
             }
             .execute()
 cancellableCollector.addCancellable(cancellable)
```

## Delete files

For delete files, just use the instance you created of the `omhStorageClient` and call method `deleteFile` sending as parameter the id of the file you want to delete.

```kotlin
 val cancellable = omhStorageClient.deleteFile(fileId)
             .addOnSuccess { result: DeleteFileUseCaseResult ->
                // The success variable indicates if the file was deleted or not
                val success: Boolean = result.isSuccess
                 // TODO - Developer: Manage success
             }
             .addOnFailure { exception: Exception ->
                 // TODO - Developer: Manage error
             }
             .execute()
 cancellableCollector.addCancellable(cancellable)
```

## Upload files

For upload files, just use the instance you created of the `omhStorageClient` and call method `uploadFile` sending as parameter the local path of the file you want to upload and the id of the remote folder where you want to place it (parent id).

```kotlin
 val cancellable = omhStorageClient.uploadFile(filePath, parentId)
             .addOnSuccess { result: UploadFileUseCaseResult ->
                // An instance of OmhFile with the information of the uploaded file. In case the file was not uploaded, will be null
                val file: OmhFile? = result.file
                 // TODO - Developer: Manage success
             }
             .addOnFailure { exception: Exception ->
                 // TODO - Developer: Manage error
             }
             .execute()
 cancellableCollector.addCancellable(cancellable)
```

## Update files

For update files, just use the instance you created of the `omhStorageClient` and call method `updateFile` sending as parameter the local path of the file you want to update and the id of the remote file you want to replace (file id).

```kotlin
 val cancellable = omhStorageClient.updateFile(filePath, fileId)
             .addOnSuccess { result: UpdateFileUseCaseResult ->
                // An instance of OmhFile with the information of the updated file. In case the file was not updated, will be null
                val file: OmhFile? = result.file
                 // TODO - Developer: Manage success
             }
             .addOnFailure { exception: Exception ->
                 // TODO - Developer: Manage error
             }
             .execute()
 cancellableCollector.addCancellable(cancellable)
```

## Download files

For download files, just use the instance you created of the `omhStorageClient` and call method `createFile` sending as parameter the id of the file you want to download and the mime type you desire to have locally (once downloaded)

```kotlin
 val cancellable = omhStorageClient.downloadFile(id, mimeTypeToSave)
             .addOnSuccess { result: DownloadFileUseCaseResult ->
                // An instance of ByteArrayOutputStream with the downloaded file
                val outputStream: ByteArrayOutputStream = result.outputStream
                 // TODO - Developer: Manage success
             }
             .addOnFailure { exception: Exception ->
                // TODO - Developer: Manage error
             }
             .execute()
 cancellableCollector.addCancellable(cancellable)
```
