package com.openmobilehub.android.storage.plugin.onedrive.restful

import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.storage.core.OmhStorageClient
import com.openmobilehub.android.storage.core.OmhStorageFactory
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.repository.OneDriveRestfulFileRepository
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.retrofit.OneDriveRetrofitImpl

class OneDriveRestfulOmhStorageClientFactory : OmhStorageFactory {
    override fun getStorageClient(authClient: OmhAuthClient): OmhStorageClient {
        val retrofit = OneDriveRetrofitImpl(authClient)

        return OneDriveRestfulOmhStorageClientImpl(
            authClient,
            OneDriveRestfulFileRepository(retrofit.apiService, retrofit.httpClient)
        )
    }
}
