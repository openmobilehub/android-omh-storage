package com.openmobilehub.android.storage.plugin.dropbox.restful

import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.storage.core.OmhStorageClient
import com.openmobilehub.android.storage.core.OmhStorageFactory
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.repository.DropboxRestfulFileRepository
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.retrofit.DropboxRetrofitImpl

class DropboxRestfulOmhStorageClientFactory : OmhStorageFactory {
    override fun getStorageClient(authClient: OmhAuthClient): OmhStorageClient {
        val retrofit = DropboxRetrofitImpl(authClient)
        val repository = DropboxRestfulFileRepository(
            retrofit.dropboxApiService,
            retrofit.dropboxContentApiService
        )
        return DropboxRestfulOmhStorageClient(authClient, repository)
    }
}
