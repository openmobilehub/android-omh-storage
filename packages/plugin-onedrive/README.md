Module plugin-onedrive

<p align="center">
  <a href="https://miniature-adventure-4gle9ye.pages.github.io/docs/">
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

### Other methods

#### Compatibility exemptions ✅❌🟨

##### OmhIdentity

| Classes     | Supported |
|-------------|:---------:|
| User        |     ✅     |
| Group       |     ✅     |
| Domain      |     ❌     |
| Anyone      |     ❌     |
| Device      |     ✅     |
| Application |     ✅     |

| User           | Supported |
|----------------|:---------:|
| id             |     ✅     |
| displayName    |     ✅     |
| emailAddress   |     🟨     |
| expirationTime |     ✅     |
| deleted        |     ❌     |
| photoLink      |     ❌     |
| pendingOwner   |     ❌     |

| Group          | Supported |
|----------------|:---------:|
| id             |     ✅     |
| displayName    |     ✅     |
| emailAddress   |     🟨     |
| expirationTime |     ✅     |
| deleted        |     ❌     |

Comments for partially supported 🟨 properties:

| Property     | Comments                    |
|--------------|-----------------------------|
| emailAddress | It may not always provided. |

##### OmhPermissionRole

| Classes   | Supported |
|-----------|:---------:|
| OWNER     |     ✅     |
| WRITER    |     ✅     |
| COMMENTER |     ❌     |
| READER    |     ✅     |

##### OmhPermissionRecipient

| Classes      | Supported |
|--------------|:---------:|
| User         |     ✅     |
| Group        |     ✅     |
| Domain       |     ❌     |
| Anyone       |     ❌     |
| WithObjectId |     ✅     |
| WithAlias    |     ✅     |

#### ⚠️ KNOWN LIMITATIONS

>
The [Sharing links](https://learn.microsoft.com/en-us/graph/api/resources/permission?view=graph-rest-1.0#sharing-links)
permissions are not supported.
