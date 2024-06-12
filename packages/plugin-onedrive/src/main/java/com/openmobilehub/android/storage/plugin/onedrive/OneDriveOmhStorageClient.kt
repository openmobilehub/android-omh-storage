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

package com.openmobilehub.android.storage.plugin.onedrive

import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.auth.core.models.OmhAuthStatusCodes
import com.openmobilehub.android.storage.core.OmhStorageClient
import com.openmobilehub.android.storage.core.model.OmhFile
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.plugin.onedrive.data.OneDriveApiClient
import com.openmobilehub.android.storage.plugin.onedrive.data.OneDriveApiService
import com.openmobilehub.android.storage.plugin.onedrive.data.OneDriveAuthProvider
import com.openmobilehub.android.storage.plugin.onedrive.repository.OneDriveFileRepository
import java.io.ByteArrayOutputStream
import java.io.File

internal class OneDriveOmhStorageClient private constructor(
    authClient: OmhAuthClient,
    private val repository: OneDriveFileRepository
) : OmhStorageClient(authClient) {

    internal class Builder : OmhStorageClient.Builder {

        override fun build(authClient: OmhAuthClient): OmhStorageClient {
            val accessToken = authClient.getCredentials().accessToken
                ?: throw OmhStorageException.InvalidCredentialsException(
                    OmhAuthStatusCodes.SIGN_IN_FAILED
                )

            val authenticationProvider = OneDriveAuthProvider(accessToken)
            val apiClient = OneDriveApiClient.getInstance(authenticationProvider)
            val apiService = OneDriveApiService(apiClient)
            val repository = OneDriveFileRepository(apiService)

            return OneDriveOmhStorageClient(authClient, repository)
        }
    }

    override val rootFolder: String
        get() = OneDriveConstants.ROOT_FOLDER // To be verified

    override suspend fun listFiles(parentId: String): List<OmhFile> {
        repository.getFilesList(parentId)
        return listOf()
    }

    override suspend fun createFile(name: String, mimeType: String, parentId: String): OmhFile? {
        // To be implemented
        return null
    }

    override suspend fun deleteFile(id: String): Boolean {
        // To be implemented
        return true
    }

    override suspend fun uploadFile(localFileToUpload: File, parentId: String?): OmhFile? {
        // To be implemented
        return null
    }

    override suspend fun downloadFile(fileId: String, mimeType: String?): ByteArrayOutputStream {
        // To be implemented
        return ByteArrayOutputStream()
    }

    override suspend fun updateFile(localFileToUpload: File, fileId: String): OmhFile? {
        // To be implemented
        return null
    }
}
