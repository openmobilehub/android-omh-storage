package com.openmobilehub.android.storage.sample.di

import android.content.Context
import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.auth.core.OmhAuthProvider
import com.openmobilehub.android.storage.core.OmhStorageClient
import com.openmobilehub.android.storage.core.OmhStorageProvider
import com.openmobilehub.android.storage.sample.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class OmhClientProvider @Inject constructor(
   @ApplicationContext private val context: Context
) {
    fun createGoogleOmhAuthClient(): OmhAuthClient {
        return OmhAuthProvider.Builder()
            .addNonGmsPath("com.openmobilehub.android.auth.plugin.google.nongms.presentation.OmhAuthFactoryImpl")
            .addGmsPath("com.openmobilehub.android.auth.plugin.google.gms.OmhAuthFactoryImpl")
            .build()
            .provideAuthClient(
                scopes = listOf("openid", "email", "profile"),
                clientId = BuildConfig.GOOGLE_CLIENT_ID,
                context = context
            )
        }

    fun createGoogleOmhStorageClient(
        omhAuthClient: OmhAuthClient
    ): OmhStorageClient {
        return OmhStorageProvider.Builder()
            .addGmsPath("com.openmobilehub.android.storage.plugin.googledrive.gms.OmhGmsStorageFactoryImpl")
            .addNonGmsPath("com.openmobilehub.android.storage.plugin.googledrive.nongms.OmhNonGmsStorageFactoryImpl")
            .build()
            .provideStorageClient(omhAuthClient, context)
    }
}