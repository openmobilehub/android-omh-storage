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

package com.openmobilehub.android.storage.plugin.dropbox

import android.webkit.MimeTypeMap
import androidx.annotation.VisibleForTesting
import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.auth.core.models.OmhAuthStatusCodes
import com.openmobilehub.android.storage.core.OmhStorageClient
import com.openmobilehub.android.storage.core.model.OmhFilePermission
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.plugin.dropbox.data.mapper.MetadataToOmhStorageEntity
import com.openmobilehub.android.storage.plugin.dropbox.data.repository.DropboxFileRepository
import com.openmobilehub.android.storage.plugin.dropbox.data.service.DropboxApiClient
import com.openmobilehub.android.storage.plugin.dropbox.data.service.DropboxApiService
import java.io.ByteArrayOutputStream
import java.io.File

internal class DropboxOmhStorageClient @VisibleForTesting internal constructor(
    authClient: OmhAuthClient,
    private val repository: DropboxFileRepository,
) : OmhStorageClient(authClient) {

    internal class Builder : OmhStorageClient.Builder {

        override fun build(authClient: OmhAuthClient): OmhStorageClient {
            val accessToken = authClient.getCredentials().accessToken
                ?: throw OmhStorageException.InvalidCredentialsException(
                    OmhAuthStatusCodes.SIGN_IN_FAILED
                )

            val client = DropboxApiClient.getInstance(accessToken)
            val apiService = DropboxApiService(client)
            val metadataToOmhStorageEntity = MetadataToOmhStorageEntity(MimeTypeMap.getSingleton())
            val repository = DropboxFileRepository(apiService, metadataToOmhStorageEntity)

            return DropboxOmhStorageClient(authClient, repository)
        }
    }

    override val rootFolder: String
        get() = DropboxConstants.ROOT_FOLDER

    override suspend fun listFiles(parentId: String): List<OmhStorageEntity> {
        return repository.getFilesList(parentId)
    }

    override suspend fun search(query: String): List<OmhStorageEntity> {
        // To be implemented
        return emptyList()
    }

    override suspend fun createFile(
        name: String,
        mimeType: String,
        parentId: String
    ): OmhStorageEntity? {
        // To be implemented
        return null
    }

    override suspend fun deleteFile(id: String): Boolean {
        // To be implemented
        return true
    }

    override suspend fun uploadFile(localFileToUpload: File, parentId: String?): OmhStorageEntity? {
        val safeParentId = parentId ?: rootFolder
        return repository.uploadFile(localFileToUpload, safeParentId)
    }

    override suspend fun downloadFile(fileId: String, mimeType: String?): ByteArrayOutputStream {
        // To be implemented
        return ByteArrayOutputStream()
    }

    override suspend fun updateFile(localFileToUpload: File, fileId: String): OmhStorageEntity? {
        // To be implemented
        return null
    }

    override suspend fun getFileVersions(fileId: String): List<OmhFileVersion> {
        // To be implemented
        return emptyList()
    }

    override suspend fun downloadFileVersion(
        fileId: String,
        versionId: String
    ): ByteArrayOutputStream {
        // To be implemented
        return ByteArrayOutputStream()
    }

    override suspend fun getFilePermissions(fileId: String): List<OmhFilePermission> {
        // To be implemented
        return emptyList()
    }
}
