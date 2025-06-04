Module plugin-dropbox-restful

<p align="center">
  <a href="https://openmobilehub.github.io/android-omh-storage/docs/">
    <img width="500px" src="https://openmobilehub.org/wp-content/uploads/sites/13/2024/06/OpenMobileHub-horizontal-color.svg"/><br/>
  </a>
  <h2 align="center">Android OMH Storage - Dropbox (RESTful)</h2>
</p>

<p align="center">
  <a href="https://central.sonatype.com/artifact/com.openmobilehub.android.storage/plugin-dropbox-restful"><img src="https://img.shields.io/maven-central/v/com.openmobilehub.android.storage/plugin-dropbox-restful" alt="NPM version"/></a>
  <a href="https://github.com/openmobilehub/android-omh-storage/blob/main/LICENSE"><img src="https://img.shields.io/github/license/openmobilehub/android-omh-storage" alt="License"/></a>
</p>

<p align="center">
  <a href="https://discord.com/invite/yTAFKbeVMw"><img src="https://img.shields.io/discord/1115727214827278446.svg?style=flat&colorA=7289da&label=Chat%20on%20Discord" alt="Chat on Discord"/></a>
  <a href="https://twitter.com/openmobilehub"><img src="https://img.shields.io/twitter/follow/rnfirebase.svg?style=flat&colorA=1da1f2&colorB=&label=Follow%20on%20Twitter" alt="Follow on Twitter"/></a>
</p>

---

Dropbox Implementation of OMH Storage API using Dropbox's own HTTP REST-ful API.

Different from plugin-dropbox, this plugin does not depend on Dropbox SDK for Java/Android, which enables usage scenarios without restrictions imposed by Dropbox SDK -

Dropbox SDK requires additional setup to allow SDK access the API credentials from within the app; this plugin allows API credentials be provisioned outside the app.

## Usage

### Set up your Dropbox application

Setup the Dropbox application from Dropbox's app console, the same as mentioned in original Dropbox plugin.

Next, you need to setup a Redirect URI. You can use Custom URI as most Android apps would do; however for better user experience you may consider Google's recommended [verified app links](https://developer.android.com/training/app-links/verify-android-applinks) method.

Apart from this, you should be able to use the plugin as-is, similar to plugin-dropbox.

### Caveats

Almost all known issues in plugin-dropbox also applies to this plugin, please refer to [plugin-dropbox's documentation](https://openmobilehub.github.io/android-omh-storage/docs/plugin-dropbox) for details.

Additionally,

- createPermission() for email addresses unknown to Dropbox may still succeed, but not visible in getFilePermissions(). You may need to go back to Dropbox web console and clear the permissions by hand. 

### Escape Hatch

This plugin does not provides an escape hatch to access the native Dropbox Android SDK, as it uses REST API instead. If needed, you can use credentials from OmhAuthClient to authorise your own REST API client.

## License

- See [LICENSE](https://github.com/openmobilehub/android-omh-storage/blob/main/LICENSE)
