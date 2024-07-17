<p align="center">
  <a href="https://miniature-adventure-4gle9ye.pages.github.io/docs/">
    <img width="500px" src="https://openmobilehub.org/wp-content/uploads/sites/13/2024/06/OpenMobileHub-horizontal-color.svg"/><br/>
  </a>
  <h2 align="center">Android OMH Storage</h2>
</p>

<p align="center">
  <a href="https://central.sonatype.com/artifact/com.openmobilehub.android.storage/core"><img src="https://img.shields.io/maven-central/v/com.openmobilehub.android.storage/core" alt="NPM version"/></a>
  <a href="https://github.com/openmobilehub/android-omh-storage/blob/main/LICENSE"><img src="https://img.shields.io/github/license/openmobilehub/android-omh-storage" alt="License"/></a>
</p>

<p align="center">
  <a href="https://discord.com/invite/yTAFKbeVMw"><img src="https://img.shields.io/discord/1115727214827278446.svg?style=flat&colorA=7289da&label=Chat%20on%20Discord" alt="Chat on Discord"/></a>
  <a href="https://twitter.com/openmobilehub"><img src="https://img.shields.io/twitter/follow/rnfirebase.svg?style=flat&colorA=1da1f2&colorB=&label=Follow%20on%20Twitter" alt="Follow on Twitter"/></a>
</p>

---

**Android OMH Storage** streamlines the integration of various cloud storage providers into your Android application by offering a unified API for different storage providers. It supports both Google Mobile Services (GMS) and non-GMS configurations, making it easy to incorporate Google Drive, OneDrive, Dropbox, and other supported third-party storage providers.

## Features

- 📱 GMS and non-GMS support for all storage providers
- 🖇️ Identical API across all storage providers
- 📦 Official storage provider SDK integration
- 🚀 Easy configuration and setup
- 💨 Lightweight modules

## OMH Storage Modules

This is the main directory of the mono-repo for Android OMH Storage. If you're searching for a particular package, please click on the corresponding package link below.

- [Core](https://miniature-adventure-4gle9ye.pages.github.io/docs/core)
- [Google Drive (GMS)](https://miniature-adventure-4gle9ye.pages.github.io/docs/plugin-googledrive-gms)
- [Google Drive (non-GMS)](https://miniature-adventure-4gle9ye.pages.github.io/docs/plugin-googledrive-non-gms)
- [OneDrive](https://miniature-adventure-4gle9ye.pages.github.io/docs/plugin-onedrive)
- [Dropbox](https://miniature-adventure-4gle9ye.pages.github.io/docs/plugin-dropbox)

## Documentation

- [Getting Started](https://miniature-adventure-4gle9ye.pages.github.io/docs/getting-started)
- [Reference API](https://miniature-adventure-4gle9ye.pages.github.io/api)

## Supported functionality

- ✅ - supported
- 🟨 - partially supported
- ❌ - not supported

| Features                | Google Drive (GMS) | Google Drive (non-GMS) | OneDrive | Dropbox |
|-------------------------|:------------------:|:----------------------:|:--------:|:-------:|
| File listing            |         ✅          |           ✅            |    ✅     |    ✅    |
| File searching          |         ✅          |           ✅            |          |    ✅    |
| File creation           |         ✅          |           ✅            |         |         |
| File update             |         ✅          |           ✅            |         |         |
| File deletion           |         ✅          |           ✅            |    ✅     |    ✅    |
| File permanent deletion |         ✅          |           ✅            |    ❌     |    ❌    |
| File upload             |         ✅          |           ✅            |    ✅     |    ✅    |
| File download           |         ✅          |           ✅            |    ✅     |    ✅    |
| File export             |         ✅          |           ✅            |    ❌     |    ❌    |
| File metadata           |         ✅          |           ✅            |    🟨     |    🟨    |
| File versioning         |         ✅          |           ✅            |    ✅     |    ✅    |
| File permissions        |         🟨          |           🟨            |    🟨     |         |
| File URL                |         ✅          |           ✅            |    ✅     |         |

### File metadata

<details markdown=1>

<summary>Show details</summary>

[`OmhStorageEntity.OmhFile`](https://miniature-adventure-4gle9ye.pages.github.io/api/packages/core/com.openmobilehub.android.storage.core.model/-omh-storage-entity/-omh-file)

| Property     | Google Drive (GMS) | Google Drive (non-GMS) | OneDrive | Dropbox |
| ------------ | :----------------: | :--------------------: | :------: | :-----: |
| id           |         ✅         |           ✅           |    ✅    |   ✅    |
| name         |         ✅         |           ✅           |    ✅    |   ✅    |
| createdTime  |         ✅         |           ✅           |    ❌    |   ❌    |
| modifiedTime |         ✅         |           ✅           |    ✅    |   ✅    |
| parentId     |         ✅         |           ✅           |    ✅    |   ✅    |
| mimeType     |         ✅         |           ✅           |    ✅    |   ✅    |
| extension    |         ✅         |           ✅           |    ✅    |   ✅    |
| size         |         ✅         |           ✅           |    ✅    |   ✅    |

[`OmhStorageEntity.OmhFolder`](https://miniature-adventure-4gle9ye.pages.github.io/api/packages/core/com.openmobilehub.android.storage.core.model/-omh-storage-entity/-omh-folder)

| Property     | Google Drive (GMS) | Google Drive (non-GMS) | OneDrive | Dropbox |
| ------------ | :----------------: | :--------------------: | :------: | :-----: |
| id           |         ✅         |           ✅           |    ✅    |   ✅    |
| name         |         ✅         |           ✅           |    ✅    |   ✅    |
| createdTime  |         ✅         |           ✅           |    ❌    |   ❌    |
| modifiedTime |         ✅         |           ✅           |    ✅    |   ❌    |
| parentId     |         ✅         |           ✅           |    ✅    |   ✅    |

[`OmhStorageMetadata.originalMetadata`](https://miniature-adventure-4gle9ye.pages.github.io/api/packages/core/com.openmobilehub.android.storage.core.model/-omh-storage-metadata/original-metadata.html)

| Storage provider       | Type                                                                                                                                                                                 |
| ---------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Google Drive (GMS)     | [`com.google.api.services.drive.model.File`](https://developers.google.com/resources/api-libraries/documentation/drive/v3/java/latest/com/google/api/services/drive/model/File.html) |
| Google Drive (non-GMS) | `String`                                                                                                                                                                             |
| OneDrive               | [`com.microsoft.graph.models.DriveItem`](https://learn.microsoft.com/en-us/graph/api/resources/driveitem#properties)                                                                 |
| Dropbox                | [`com.dropbox.core.v2.files.Metadata`](https://dropbox.github.io/dropbox-sdk-java/api-docs/v2.0.x/com/dropbox/core/v2/files/Metadata.html)                                           |

</details>

### File versioning

<details markdown=1>

<summary>Show details</summary>

[`OmhFileVersion`](https://miniature-adventure-4gle9ye.pages.github.io/api/packages/core/com.openmobilehub.android.storage.core.model/-omh-file-version)

| Property     | Google Drive (GMS) | Google Drive (non-GMS) | OneDrive | Dropbox |
| ------------ | :----------------: | :--------------------: | :------: | :-----: |
| fieldId      |         ✅         |           ✅           |    ✅    |   ✅    |
| versionId    |         ✅         |           ✅           |    ✅    |   ✅    |
| lastModified |         ✅         |           ✅           |    ✅    |   ✅    |

</details>

### File permissions

<details markdown=1>

<summary>Show details</summary>

[`OmhPermission.IdentityPermission`](https://miniature-adventure-4gle9ye.pages.github.io/api/packages/core/com.openmobilehub.android.storage.core.model/-omh-permission)

| Property            | Google Drive (GMS) | Google Drive (non-GMS) | OneDrive | Dropbox |
|---------------------|:------------------:|:----------------------:|:--------:|:-------:|
| id                  |         ✅         |           ✅           |    ✅    |         |
| role                |         ✅         |           ✅           |    ✅    |         |
| identity            |         ✅         |           ✅           |    ✅    |         |
| inheritedFromEntity |         🟨         |           🟨           |    ✅    |         |

> Google Drive: `inheritedFromEntity` is present only for shared drive items.

[`OmhIdentity`](https://miniature-adventure-4gle9ye.pages.github.io/api/packages/core/com.openmobilehub.android.storage.core.model/-omh-identity)

| Type        | Google Drive (GMS) | Google Drive (non-GMS) | OneDrive | Dropbox |
| ----------- | :----------------: | :--------------------: | :------: | :-----: |
| User        |         ✅         |           ✅           |    ✅    |         |
| Group       |         ✅         |           ✅           |    ✅    |         |
| Domain      |         ✅         |           ✅           |    ❌    |         |
| Anyone      |         ✅         |           ✅           |    ❌    |         |
| Device      |         ❌         |           ❌           |    ✅    |         |
| Application |         ❌         |           ❌           |    ✅    |         |

[`OmhIdentity.User`](https://miniature-adventure-4gle9ye.pages.github.io/api/packages/core/com.openmobilehub.android.storage.core.model/-omh-identity/-user)

| Property       | Google Drive (GMS) | Google Drive (non-GMS) | OneDrive | Dropbox |
| -------------- | :----------------: | :--------------------: | :------: | :-----: |
| id             |         ❌         |           ❌           |    ✅    |         |
| displayName    |         ✅         |           ✅           |    ✅    |         |
| emailAddress   |         ✅         |           ✅           |    ❌    |         |
| expirationTime |         ✅         |           ✅           |    ✅    |         |
| deleted        |         ✅         |           ✅           |    ❌    |         |
| photoLink      |         ✅         |           ✅           |    ❌    |         |
| pendingOwner   |         ❌         |           ✅           |    ❌    |         |

[`OmhIdentity.Group`](https://miniature-adventure-4gle9ye.pages.github.io/api/packages/core/com.openmobilehub.android.storage.core.model/-omh-identity/-group)

| Property       | Google Drive (GMS) | Google Drive (non-GMS) | OneDrive | Dropbox |
| -------------- | :----------------: | :--------------------: | :------: | :-----: |
| id             |         ❌         |           ❌           |    ✅    |         |
| displayName    |         ✅         |           ✅           |    ✅    |         |
| emailAddress   |         ✅         |           ✅           |    ❌    |         |
| expirationTime |         ✅         |           ✅           |    ✅    |         |
| deleted        |         ✅         |           ✅           |    ❌    |         |

[`OmhPermissionRole`](https://miniature-adventure-4gle9ye.pages.github.io/api/packages/core/com.openmobilehub.android.storage.core.model/-omh-permission-role)

| Role      | Google Drive (GMS) | Google Drive (non-GMS) | OneDrive | Dropbox |
| --------- | :----------------: | :--------------------: | :------: | :-----: |
| OWNER     |         ✅         |           ✅           |    ✅    |         |
| WRITER    |         ✅         |           ✅           |    ✅    |         |
| COMMENTER |         ✅         |           ✅           |    ❌    |         |
| READER    |         ✅         |           ✅           |    ✅    |         |

[`OmhPermissionRecipient`](https://miniature-adventure-4gle9ye.pages.github.io/api/packages/core/com.openmobilehub.android.storage.core.model/-omh-permission-recipient)

| Type         | Google Drive (GMS) | Google Drive (non-GMS) | OneDrive | Dropbox |
| ------------ | :----------------: | :--------------------: | :------: | :-----: |
| User         |         ✅         |           ✅           |    ✅    |         |
| Group        |         ✅         |           ✅           |    ✅    |         |
| Domain       |         ✅         |           ✅           |    ❌    |         |
| Anyone       |         ✅         |           ✅           |    ❌    |         |
| WithObjectId |         ❌         |           ❌           |    ✅    |         |
| WithAlias    |         ❌         |           ❌           |    ✅    |         |

</details>

## Contributing

- [Overview](https://github.com/openmobilehub/android-omh-storage/blob/main/CONTRIBUTING.md)
- [Issues](https://github.com/openmobilehub/android-omh-storage/issues)
- [PRs](https://github.com/openmobilehub/android-omh-storage/pulls)

## License

- See [LICENSE](https://github.com/openmobilehub/android-omh-storage/blob/main/LICENSE)
