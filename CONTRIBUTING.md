# Contributing to Android OMH Storage

Thanks for your help! We currently review PRs for the **packages/**, **docs/**, and **apps/** directories, as well as **markdown** files.

## Sample app setup

We recommend that anyone interested in contributing to this library use the sample app located in [**apps/storage-sample**](https://github.com/openmobilehub/android-omh-storage/blob/main/apps/storage-sample) as part of their development workflow when introducing or testing changes. Please follow the [sample app setup guide](https://github.com/openmobilehub/android-omh-storage/blob/main/apps/storage-sample/README.md) for detailed instructions.

## Before submitting your PR

### Commit messages

If this is your first time committing to a large public repo, you might find this tutorial helpful: [How to Write a Git Commit Message](https://cbea.ms/git-commit)

### Testing

To increase the chances of your changes being merged, ensure you build good tests for them. Each module in **packages/** has unit tests located in the respective **test/** directory under that module. Make sure your changes are covered by these unit tests to help maintain the stability of Android OMH Storage for everyone.

You can run all the tests with the following command:

```bash
./gradlew test
```

### Linting

It's essential to check for linting issues before submitting your PR. You can run the following command to ensure your code adheres to the project's style guidelines:

```bash
./gradlew detekt
```

### Assembling

Ensure your changes compile correctly by assembling the project. You can do this with the following command:

```bash
./gradlew assemble
```

## Updating documentation

This project maintains documentation across several important areas:

- Project Overview - [**/README.md**](/README.md)
- Getting Started - [**/docs/markdown/getting-started.md**](/docs/markdown/getting-started.md)
- Core Plugin - [**/packages/core/README.md**](/packages/core/README.md)
- Google Drive Plugin - [**/packages/plugin-googledrive-gms/README.md**](/packages/plugin-googledrive-gms/README.md)
- OneDrive Plugin - [**/packages/plugin-onedrive/README.md**](/packages/plugin-onedrive/README.md)
- Dropbox Plugin - [**/packages/plugin-dropbox/README.md**](/packages/plugin-dropbox/README.md)
- Contributing Guidelines - [**/CONTRIBUTING.md**](/CONTRIBUTING.md)
- Sample App Setup - [**/apps/storage-sample/README.md**](/apps/storage-sample/README.md)
- License - [**/LICENSE.md**](/LICENSE.md)

### Building documentation locally

You can build the documentation locally with the following command:

```bash
./gradlew buildDocs
```

This command performs the following Gradle tasks:

- `dokkaHtmlMultiModule` - Generates HTML API documentation for all modules. Outputs are written to **/docs/generated/**.

- `copyMarkdownDocs` - Copies and sanitizes markdown files from **packages/\<name\>/docs/** to be processed by Jekyll in **/docs/markdown/**.

#### Project documentation

To locally serve the entire project documentation, ensure [**Ruby 3.1.6**](https://www.ruby-lang.org/en/news/2024/05/29/ruby-3-1-6-released) is installed on your machine. Run a local Jekyll server by executing `bundle exec jekyll serve` inside **/docs/markdown**. Note that each time you modify documentation in any package, you will have to run the `copyMarkdownDocs` Gradle task to update Jekyll with the new markdown files.

#### API documentation

To view the generated API documentation locally, serve the directory **/docs/generated/** using a local server of your choice (e.g., `python3 -m http.server`).

## Creating a plugin

To create a new plugin for a storage provider, follow these steps:

### 1. Add dependency for the Core package

Add the dependency for the Core package to your plugin's **build.gradle** file:

```gradle
dependencies {
  implementation("com.openmobilehub.android.storage:core:<version>")
}
```

### 2. Implement the `OmhStorageClient` class

To implement the [`OmhStorageClient`](https://openmobilehub.github.io/android-omh-storage/api/packages/core/com.openmobilehub.android.storage.core/-omh-storage-client) class, you will need to provide implementations for various functionalities specific to your storage provider. Use the existing plugins as references to guide you in developing your own plugin.

Here are some examples to help structure your implementation and integrate with different storage providers:

- [Google Drive (GMS)](https://github.com/openmobilehub/android-omh-storage/tree/main/packages/plugin-googledrive-gms)
- [Google Drive (non-GMS)](https://github.com/openmobilehub/android-omh-storage/tree/main/packages/plugin-googledrive-non-gms)
- [OneDrive](https://github.com/openmobilehub/android-omh-storage/tree/main/packages/plugin-onedrive)
- [Dropbox](https://github.com/openmobilehub/android-omh-storage/tree/main/packages/plugin-dropbox)

By examining these examples, you can better understand the best practices and necessary components for creating a robust plugin tailored to your storage provider.
