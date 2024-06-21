/*
 * Copyright 2023 Open Mobile Hub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.openmobilehub.android.storage.plugin.dropbox.data.service

import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.openmobilehub.android.storage.plugin.dropbox.BuildConfig

internal class DropboxApiClient private constructor(internal val accessToken: String) {
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
