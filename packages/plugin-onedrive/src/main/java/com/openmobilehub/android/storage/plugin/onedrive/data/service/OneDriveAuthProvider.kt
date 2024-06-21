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

package com.openmobilehub.android.storage.plugin.onedrive.data.service

import com.microsoft.kiota.RequestInformation
import com.microsoft.kiota.authentication.AuthenticationProvider

class OneDriveAuthProvider(internal val accessToken: String) : AuthenticationProvider {
    override fun authenticateRequest(
        request: RequestInformation,
        additionalAuthenticationContext: MutableMap<String, Any>?
    ) {
        request.headers.add("Authorization", "Bearer $accessToken")
    }
}
