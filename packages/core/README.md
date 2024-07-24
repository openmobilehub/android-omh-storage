Module core

<p align="center">
  <a href="https://miniature-adventure-4gle9ye.pages.github.io/docs/">
    <img width="500px" src="https://openmobilehub.org/wp-content/uploads/sites/13/2024/06/OpenMobileHub-horizontal-color.svg"/><br/>
  </a>
  <h2 align="center">Android OMH Storage - Core</h2>
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

## Installation

To integrate the Core package into your Android project, follow these steps:

### 1. Configure Maven Central repository

Ensure Maven Central is included as a repository in your root **build.gradle** file:

```gradle
allprojects {
  repositories {
    mavenCentral()
  }
}
```

### 2. Add dependency for the Core package

Add the dependency for the Core package to your project's **build.gradle** file:

```gradle
dependencies {
  implementation("com.openmobilehub.android.storage:core:2.0.0")
}
```

## License

- See [LICENSE](https://github.com/openmobilehub/android-omh-storage/blob/main/LICENSE)
