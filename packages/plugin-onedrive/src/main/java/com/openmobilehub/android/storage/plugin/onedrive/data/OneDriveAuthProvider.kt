package com.openmobilehub.android.storage.plugin.onedrive.data

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
