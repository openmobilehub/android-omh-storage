package com.openmobilehub.android.storage.sample.di

import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.storage.core.OmhStorageClient
import javax.inject.Inject

class OmhClientManager @Inject constructor(
    private val omhClientProvider: OmhClientProvider
) {
    private var googleOmhAuthClient: OmhAuthClient = omhClientProvider.createGoogleOmhAuthClient()

    fun getAuthClient(): OmhAuthClient = googleOmhAuthClient
    fun getStorageClient(): OmhStorageClient = omhClientProvider.createGoogleOmhStorageClient(googleOmhAuthClient)
}