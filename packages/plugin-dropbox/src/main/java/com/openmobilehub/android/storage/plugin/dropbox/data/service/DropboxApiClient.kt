package com.openmobilehub.android.storage.plugin.dropbox.data.service

import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.openmobilehub.android.storage.plugin.dropbox.BuildConfig

class DropboxApiClient(internal val accessToken: String) {
    companion object {
        private var instance: DropboxApiClient? = null

        internal fun getInstance(newAccessToken: String): DropboxApiClient {
            val oldAccessToken = instance?.accessToken
            val isDifferentAccount = oldAccessToken != newAccessToken
            if (instance == null || isDifferentAccount) {
                instance = DropboxApiClient(newAccessToken)
            }

            return instance!!
        }
    }

    private val applicationName = BuildConfig.LIBRARY_PACKAGE_NAME

    internal val dropboxApiService: DbxClientV2 by lazy { initDropboxApiService() }

    private fun initDropboxApiService(): DbxClientV2 {
        val config = DbxRequestConfig.newBuilder(applicationName).build()
        return DbxClientV2(config, accessToken)
    }
}
