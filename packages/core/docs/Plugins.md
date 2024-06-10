---
title: Plugins
layout: default
parent: Core
---

For creating a custom implementation of our OMH Storage interfaces you'll need to get our OMH Storage API dependency:

```groovy
implementation 'com.openmobilehub.android:storage-api:$version'
```

Once you've downloaded the dependencies, it's time to extend each interface. Below you can find a step by step guide on how to do it.

# Developing your implementation

## Implementing the OMH Storage Client

As explained in the [OMH Storage Client page](https://github.com/openmobilehub/omh-storage/wiki/OMH-Storage-Client), this will be the main interactor for the library. You will need to provide an implementation for each of the functionalities so that the developers can interact with the Storage provider you'll be implementing. For achieve this, you must implement 3 classes: `OmhStorageClient`, `OmhFileRepository` and `OmhFileRemoteDataSource`

Here you must ensure the function `getRepository(): OmhFileRepository` from your client implementation returns your implementation of `OmhFileRepository` and also this repository should have a reference to your implementation of `OmhFileRemoteDataSource`

## Implementing the OMH Storage Factory

This will be the most important part of your implementation as this will be how we reflect your library with the provider. Save the path to this class, as we'll be using it further down the line. This class will be responsible of instantiating your OMH Storage Client implementation and returning it to the user as the abstract `OmhStorageClient`. The only function to implement here is:

```kotlin
fun getStorageClient(authClient: OmhAuthClient): OmhStorageClient
```

**Note: here are some examples of the path that you should be storing: `com.example.app.factories.MyCustomFactoryImplementation`**

## Implementing the OMH Tasks abstract class

This is an abstraction for the async layer of your library. The idea is to avoid forcing the user to use a specific async library and give the more flexibility with your OMH Auth implementation. You can read more about it [here](https://github.com/openmobilehub/omh-auth/wiki/OMH-Task). Here the only function you need to implement is:

```kotlin
abstract fun execute(): OmhCancellable?
```

This should execute your async code and return a way to cancel the operation with the `OmhCancellable` interface if possible. The cancellable interface can be represented as a lambda for convenience.

# Using your implementation with the OMH Core Plugin

To use your newly created implementation with out plugin you just need to pass the reflection path and the dependency string in the `Service` section like this:

```groovy
omhConfig {
   bundle("gms") {
      storage {
         gmsService {
            dependency = "com.example.app:custom-implementation:1.0"
            path = "com.example.app.factories.MyOwnFactoryImplementation"
         }
      }
   }
}
```

# `STORAGE_GMS_PATH` and `STORAGE_NON_GMS_PATH`

The factory implementation is provided using reflection. When a developer provides it's own factory implementation is necessary to configure which path will be used for this purpose. In this way the purpose of `STORAGE_GMS_PATH` and `STORAGE_NON_GMS_PATH` constants on the gradle file is set the path of the custom factory that will be used by the library.

In this way, they're constructed during compile time to avoid hardcoding the factory path. Also have in mind, if you modify the path's value is necessary rebuild the project to apply the change.

# Resources

You can always look into our own implementations of the OMH Storage API ([GMS](https://github.com/openmobilehub/omh-storage/tree/main/storage-api-drive-gms) and [non GMS](https://github.com/openmobilehub/omh-storage/tree/main/storage-api-drive-nongms) as a reference to help you develop your own implementation.
