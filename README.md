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

| Features                | Google Drive GMS | Google Drive non-GMS | OneDrive | Dropbox |
| ----------------------- | :--------------: | :------------------: | :------: | :-----: |
| File listing            |        ✅        |          ✅          |    ✅    |   ✅    |
| File searching          |        ✅        |          ✅          |          |   ✅    |
| File creation           |        ✅        |          ✅          |          |         |
| File update             |        ✅        |          ✅          |          |         |
| File deletion           |        ✅        |          ✅          |    ✅    |   ✅    |
| File permanent deletion |        ✅        |          ✅          |    ❌    |   ❌    |
| File upload             |        ✅        |          ✅          |    ✅    |   ✅    |
| File download           |        ✅        |          ✅          |    ✅    |   ✅    |
| File export             |        ✅        |          ✅          |    ❌    |   ❌    |
| File metadata           |        ✅        |          ✅          |    ✅    |   ✅    |
| File versioning         |        ✅        |          ✅          |    ✅    |   ✅    |
| File permissions        |        ✅        |          ✅          |    ✅    |         |
| File URL                |        ✅        |          ✅          |          |         |

### File permissions

`OmhIdentity`

| Type        | Google Drive GMS | Google Drive non-GMS | OneDrive | Dropbox |
| ----------- | :--------------: | :------------------: | :------: | :-----: |
| User        |        ✅        |          ✅          |    ✅    |         |
| Group       |        ✅        |          ✅          |    ✅    |         |
| Domain      |        ✅        |          ✅          |    ❌    |         |
| Anyone      |        ✅        |          ✅          |    ❌    |         |
| Device      |        ❌        |          ❌          |    ✅    |         |
| Application |        ❌        |          ❌          |    ✅    |         |

`OmhIdentity.User`

| Property       | Google Drive GMS | Google Drive non-GMS | OneDrive | Dropbox |
| -------------- | :--------------: | :------------------: | :------: | :-----: |
| id             |        ❌        |          ❌          |    ✅    |         |
| displayName    |        ✅        |          ✅          |    ✅    |         |
| emailAddress   |        ✅        |          ✅          |    🟨    |         |
| expirationTime |        ✅        |          ✅          |    ✅    |         |
| deleted        |        ✅        |          ✅          |    ❌    |         |
| photoLink      |        ✅        |          ✅          |    ❌    |         |
| pendingOwner   |        ❌        |          ✅          |    ❌    |         |

`OmhIdentity.Group`

| Property       | Google Drive GMS | Google Drive non-GMS | OneDrive | Dropbox |
| -------------- | :--------------: | :------------------: | :------: | :-----: |
| id             |        ❌        |          ❌          |    ✅    |         |
| displayName    |        ✅        |          ✅          |    ✅    |         |
| emailAddress   |        ✅        |          ✅          |    🟨    |         |
| expirationTime |        ✅        |          ✅          |    ✅    |         |
| deleted        |        ✅        |          ✅          |    ❌    |         |

`OmhPermissionRole`

| Role      | Google Drive GMS | Google Drive non-GMS | OneDrive | Dropbox |
| --------- | :--------------: | :------------------: | :------: | :-----: |
| OWNER     |        ✅        |          ✅          |    ✅    |         |
| WRITER    |        ✅        |          ✅          |    ✅    |         |
| COMMENTER |        ✅        |          ✅          |    ❌    |         |
| READER    |        ✅        |          ✅          |    ✅    |         |

`OmhPermissionRecipient`

| Type         | Google Drive GMS | Google Drive non-GMS | OneDrive | Dropbox |
| ------------ | :--------------: | :------------------: | :------: | :-----: |
| User         |        ✅        |          ✅          |    ✅    |         |
| Group        |        ✅        |          ✅          |    ✅    |         |
| Domain       |        ✅        |          ✅          |    ❌    |         |
| Anyone       |        ✅        |          ✅          |    ❌    |         |
| WithObjectId |        ❌        |          ❌          |    ✅    |         |
| WithAlias    |        ❌        |          ❌          |    ✅    |         |

#### 🟨 Caveats

> The `emailAddress` property may not always be provided by the OneDrive storage provider.

## Contributing

- [Overview](https://github.com/openmobilehub/android-omh-storage/blob/main/CONTRIBUTING.md)
- [Issues](https://github.com/openmobilehub/android-omh-storage/issues)
- [PRs](https://github.com/openmobilehub/android-omh-storage/pulls)

## License

- See [LICENSE](https://github.com/openmobilehub/android-omh-storage/blob/main/LICENSE)
