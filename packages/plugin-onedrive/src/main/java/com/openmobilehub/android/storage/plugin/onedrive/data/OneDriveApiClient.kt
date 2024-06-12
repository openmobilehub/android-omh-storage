package com.openmobilehub.android.storage.plugin.onedrive.data

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

//    internal val oneDriveApiClient: GraphServiceClient by lazy { initOneDriveApiClient() }

//    private fun initOneDriveApiClient() = GraphServiceClient(authProvider)
}
