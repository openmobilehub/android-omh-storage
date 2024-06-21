---
title: OMH App Data
layout: default
parent: Core
---

# Store application-specific data

The application data folder is a special hidden folder that your app can use to store application-specific data, such as configuration files. The application data folder is automatically created when you attempt to create a file in it. Use this folder to store any files that the user shouldn't directly interact with. This folder is only accessible by your application and its contents are hidden from the user and from other Drive apps. For more information refer to [appData](https://developers.google.com/drive/api/guides/appdata).

## Application data folder scope

Before you can access the application data folder, you must request access to the `https://www.googleapis.com/auth/drive.appdata` scope. For more information about scopes and how to request access to them, refer to [API-specific authorization and authentication information](https://developers.google.com/drive/api/guides/api-specific-auth). For more information about specific OAuth 2.0 scopes, see [OAuth 2.0 Scopes for Google APIs](https://developers.google.com/identity/protocols/oauth2/scopes#drive).

```kotlin
.provideAuthClient(
    context = context,
    scopes = listOf(
        "openid",
        "email",
        "profile",
        "https://www.googleapis.com/auth/drive",
        "https://www.googleapis.com/auth/drive.file",
        "https://www.googleapis.com/auth/drive.appdata"
    ),
    clientId = BuildConfig.CLIENT_ID
)
```

## Upload a file in the application data folder

To upload a file in the application data folder, specify `appDataFolder` in the parents property of the file and use the `OmhStorageClient.uploadFile` method to upload the file to the folder. The following code sample shows how to insert a file into a folder using the OMH Storage SDK.

```kotlin
import com.omh.android.auth.api.async.CancellableCollector
import com.omh.android.storage.api.OmhStorageClient
import com.omh.android.storage.api.domain.model.OmhStorageEntity
import java.io.File

...

val cancellableCollector = CancellableCollector()

fun uploadFile(omhStorageClient: OmhStorageClient) {
    val fileName = "config.json"
    val parentId = "appDataFolder"
    val filePath = File("files/config.json", fileName)

    val cancellable = omhStorageClient.uploadFile(filePath, parentId)
        .addOnSuccess { result ->
            if (result.file == null) {
                // Handle the file was not uploaded.
            } else {
                val uploadedFile: OmhFile? = result.file
                // Handle the uploadedFile as needed.
            }
        }
        .addOnFailure { exception: Exception ->
            // Handle the exception.
            exception.printStackTrace()
        }
        .execute()

    cancellableCollector.addCancellable(cancellable)
}

override fun onDestroy() {
     super.onDestroy()
     cancellableCollector.clear()
}
```
