package com.openmobilehub.android.storage.sample.di

import android.content.Context
import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.auth.core.OmhAuthProvider
import com.openmobilehub.android.auth.plugin.dropbox.DropboxAuthClient
import com.openmobilehub.android.auth.plugin.microsoft.MicrosoftAuthClient
import com.openmobilehub.android.storage.sample.BuildConfig
import com.openmobilehub.android.storage.sample.R
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
class AuthModule {
    @Provides
    fun provideOmhAuthClient(
        @Named("google") googleAuthClient: Provider<OmhAuthClient>,
        dropboxAuthClient: Provider<DropboxAuthClient>,
        microsoftAuthClient: Provider<MicrosoftAuthClient>,
        sessionRepository: SessionRepository
    ): OmhAuthClient {
        return when (sessionRepository.getStorageAuthProvider()) {
            StorageAuthProvider.GOOGLE -> googleAuthClient.get()
            StorageAuthProvider.DROPBOX -> dropboxAuthClient.get()
            StorageAuthProvider.MICROSOFT -> microsoftAuthClient.get()
        }
    }

    @Named("google")
    @Provides
    @Singleton
    fun providesGoogleOmhAuthClient(@ApplicationContext context: Context): OmhAuthClient {
        return OmhAuthProvider.Builder()
            .addNonGmsPath("com.openmobilehub.android.auth.plugin.google.nongms.presentation.OmhAuthFactoryImpl")
            .addGmsPath("com.openmobilehub.android.auth.plugin.google.gms.OmhAuthFactoryImpl")
            .build()
            .provideAuthClient(
                context = context,
                scopes = listOf(
                    "openid",
                    "email",
                    "profile",
                    "https://www.googleapis.com/auth/drive",
                    "https://www.googleapis.com/auth/drive.file"
                ),
                clientId = BuildConfig.GOOGLE_CLIENT_ID
            )
    }

    @Provides
    @Singleton
    fun providesDropboxAuthClient(@ApplicationContext context: Context): DropboxAuthClient {
        return DropboxAuthClient(
            scopes = arrayListOf("account_info.read", "files.metadata.read"),
            context = context,
            appId = BuildConfig.DROPBOX_APP_KEY,
        )
    }

    @Provides
    @Singleton
    fun providesMicrosoftAuthClient(@ApplicationContext context: Context): MicrosoftAuthClient {
        return MicrosoftAuthClient(
            configFileResourceId = R.raw.ms_auth_config,
            context = context,
            scopes = arrayListOf("User.Read", "Files.ReadWrite.All"),
        )
    }
}