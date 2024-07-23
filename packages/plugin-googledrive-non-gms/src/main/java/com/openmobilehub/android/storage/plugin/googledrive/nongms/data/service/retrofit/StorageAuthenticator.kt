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

package com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.retrofit

import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.utils.accessToken
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

internal class StorageAuthenticator(private val omhAuthClient: OmhAuthClient) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshedToken = omhAuthClient.accessToken ?: return null
        return response.request
            .newBuilder()
            .header(
                name = GoogleStorageApiServiceProvider.HEADER_AUTHORIZATION_NAME,
                value = GoogleStorageApiServiceProvider.BEARER.format(refreshedToken)
            )
            .build()
    }
}
