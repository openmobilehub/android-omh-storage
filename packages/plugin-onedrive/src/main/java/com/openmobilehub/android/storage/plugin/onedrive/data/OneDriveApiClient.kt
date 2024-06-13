package com.openmobilehub.android.storage.plugin.onedrive.data

import com.microsoft.graph.serviceclient.GraphServiceClient
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.model.OmhStorageStatusCodes

class OneDriveApiClient(private val authProvider: OneDriveAuthProvider) {
    companion object {
        private var instance: OneDriveApiClient? = null

        internal fun getInstance(newAuthProvider: OneDriveAuthProvider): OneDriveApiClient {
            val oldAuthProvider = instance?.authProvider
            val isDifferentAccount = oldAuthProvider?.accessToken != newAuthProvider.accessToken

            if (instance == null || isDifferentAccount) {
                instance = OneDriveApiClient(newAuthProvider)
            }

            return instance!!
        }
    }

    internal val graphServiceClient = GraphServiceClient(authProvider)
    internal val driveId by lazy { getDriveId() }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private fun getDriveId(): String {
        try {
            return graphServiceClient.me().drive().get().id
        } catch (e: Exception) {
            throw OmhStorageException.ApiException(OmhStorageStatusCodes.ROOT_FOLDER_ERROR)
        }
    }
}
