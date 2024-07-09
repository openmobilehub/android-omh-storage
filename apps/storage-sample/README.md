# Sample app

We recommend that anyone interested in contributing to this library use this sample app as part of their development workflow when introducing or testing changes.

## Prerequisites

- **Gradle version**: 7.0 or higher
- **Android API level**: 26 or higher

## Storage provider configuration

Each storage provider requires specific secrets for configuration. Please follow the individual storage provider configuration guides:

- [Google Drive](https://miniature-adventure-4gle9ye.pages.github.io/docs/plugin-googledrive-gms/#configuration)
- [OneDrive](https://miniature-adventure-4gle9ye.pages.github.io/docs/plugin-onedrive/#configuration)
- [Dropbox](https://miniature-adventure-4gle9ye.pages.github.io/docs/plugin-dropbox/#configuration)

## Dependencies setup

For using the plugins for development locally, there are two approaches:

### Using local modules (recommended)

In this scenario, you can use local modules (sub-projects) located inside the **packages/** directory instead of relying on Maven Local dependencies. This setup allows you to modify the code in a plugin and see the changes instantly in the sample app without needing to publish to Maven Local.

To use local modules, add the following entry to your project's **local.properties** file:

```bash
useLocalProjects=true
```

Additionally you can add it as a flag to your `gradlew` command:

```bash
./gradlew -P useLocalProjects=true ...
```

### Using Maven Local

To use Maven Local, add the following entry to your project's **local.properties** file:

```bash
useMavenLocal=true
```

Additionally you can add it as a flag to your `gradlew` command:

```bash
./gradlew -P useMavenLocal=true ...
```

#### Publishing modules to Maven Local

When you make changes to any module within the **packages/** directory, you need to publish the specific module to Maven Local to reflect those changes.

##### Step 1: Publish the `Core` module to Maven Local

Before publishing other modules, you must publish the `core` module first by running the following command:

```bash
./gradlew :packages:core:publishToMavenLocal
```

This ensures that any dependencies on the `core` module are resolved correctly.

##### Step 2: Publish other modules to Maven Local

After publishing the `core` module, you can publish the remaining modules. You have two options:

1. To publish all modules:

```bash
# From the root directory
./gradlew publishToMavenLocal
```

2. To publish a specific module:

```bash
./gradlew :packages:{module}:publishToMavenLocal
```

## License

- See [LICENSE](https://github.com/openmobilehub/android-omh-storage/blob/main/LICENSE)
