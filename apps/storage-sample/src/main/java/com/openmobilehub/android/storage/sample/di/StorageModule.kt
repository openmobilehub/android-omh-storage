package com.openmobilehub.android.storage.sample.di

import android.content.Context
import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.storage.core.OmhStorageClient
import com.openmobilehub.android.storage.core.OmhStorageProvider
import com.openmobilehub.android.storage.plugin.dropbox.DropboxOmhStorageFactory
import com.openmobilehub.android.storage.plugin.googledrive.gms.GoogleDriveGmsConstants
import com.openmobilehub.android.storage.plugin.googledrive.nongms.GoogleDriveNonGmsConstants
import com.openmobilehub.android.storage.plugin.onedrive.OneDriveOmhStorageFactory
import com.openmobilehub.android.storage.sample.domain.model.StorageAuthProvider
import com.openmobilehub.android.storage.sample.domain.repository.SessionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class StorageModule {

    @Provides
    fun providesOmhStorageClient(
        @Named("google") googleStorageClient: Provider<OmhStorageClient>,
        @Named("dropbox") dropboxStorageClient: Provider<OmhStorageClient>,
        @Named("microsoft") microsoftStorageClient: Provider<OmhStorageClient>,
        sessionRepository: SessionRepository
    ): OmhStorageClient {
        return when (sessionRepository.getStorageAuthProvider()) {
            StorageAuthProvider.GOOGLE -> googleStorageClient.get()
            StorageAuthProvider.DROPBOX -> dropboxStorageClient.get()
            StorageAuthProvider.MICROSOFT -> microsoftStorageClient.get()
        }
    }

    @Named("google")
    @Provides
    @Singleton
    fun providesGoogleOmhStorageClient(
        @ApplicationContext context: Context,
        omhAuthClient: OmhAuthClient
    ): OmhStorageClient {
        return OmhStorageProvider.Builder()
            .addGmsPath(GoogleDriveGmsConstants.IMPLEMENTATION_PATH)
            .addNonGmsPath(GoogleDriveNonGmsConstants.IMPLEMENTATION_PATH)
            .build()
            .provideStorageClient(omhAuthClient, context)
    }

    @Named("dropbox")
    @Provides
    @Singleton
    fun providesDropboxOmhStorageClient(omhAuthClient: OmhAuthClient): OmhStorageClient {
        return DropboxOmhStorageFactory().getStorageClient(omhAuthClient)
    }

    @Named("microsoft")
    @Provides
    @Singleton
    fun providesMicrosoftOmhStorageClient(omhAuthClient: OmhAuthClient): OmhStorageClient {
        return OneDriveOmhStorageFactory().getStorageClient(omhAuthClient)
    }

}