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

import android.webkit.MimeTypeMap
import androidx.annotation.VisibleForTesting
import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.storage.core.OmhStorageClient
import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.plugin.onedrive.data.mapper.DriveItemToOmhStorageEntity
import com.openmobilehub.android.storage.plugin.onedrive.data.repository.OneDriveFileRepository
import com.openmobilehub.android.storage.plugin.onedrive.data.service.OneDriveApiClient
import com.openmobilehub.android.storage.plugin.onedrive.data.service.OneDriveApiService
import com.openmobilehub.android.storage.plugin.onedrive.data.service.OneDriveAuthProvider
import java.io.ByteArrayOutputStream
import java.io.File

@Suppress("TooManyFunctions")
internal class OneDriveOmhStorageClient @VisibleForTesting internal constructor(
    authClient: OmhAuthClient,
    private val repository: OneDriveFileRepository
) : OmhStorageClient(authClient) {

    internal class Builder : OmhStorageClient.Builder {

        override fun build(authClient: OmhAuthClient): OmhStorageClient {
            val accessToken = authClient.getCredentials().accessToken
                ?: throw OmhStorageException.InvalidCredentialsException()

            val authProvider = OneDriveAuthProvider(accessToken)
            val apiClient = OneDriveApiClient.getInstance(authProvider)
            val apiService = OneDriveApiService(apiClient)
            val driveItemToOmhStorageEntity =
                DriveItemToOmhStorageEntity(MimeTypeMap.getSingleton())
            val repository = OneDriveFileRepository(apiService, driveItemToOmhStorageEntity)

            return OneDriveOmhStorageClient(authClient, repository)
        }
    }

    override val rootFolder: String
        get() = OneDriveConstants.ROOT_FOLDER

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

    override suspend fun permanentlyDeleteFile(id: String): Boolean {
        // To be implemented
        return false
    }

    override suspend fun uploadFile(localFileToUpload: File, parentId: String?): OmhStorageEntity? {
        val safeParentId = parentId ?: rootFolder
        return repository.uploadFile(localFileToUpload, safeParentId)
    }

    override suspend fun downloadFile(fileId: String, mimeType: String?): ByteArrayOutputStream {
        return repository.downloadFile(fileId)
    }

    override suspend fun updateFile(
        localFileToUpload: File,
        fileId: String
    ): OmhStorageEntity.OmhFile? {
        // To be implemented
        return null
    }

    override suspend fun getFileVersions(fileId: String): List<OmhFileVersion> {
        return repository.getFileVersions(fileId)
    }

    override suspend fun downloadFileVersion(
        fileId: String,
        versionId: String
    ): ByteArrayOutputStream {
        return repository.downloadFileVersion(fileId, versionId)
    }

    override suspend fun getFilePermissions(fileId: String): List<OmhPermission> {
        // To be implemented
        return emptyList()
    }

    override suspend fun deletePermission(fileId: String, permissionId: String): Boolean {
        // To be implemented
        return true
    }

    override suspend fun updatePermission(
        fileId: String,
        permissionId: String,
        role: OmhPermissionRole
    ): OmhPermission {
        TODO("Not yet implemented")
    }

    override suspend fun createPermission(
        fileId: String,
        permission: OmhCreatePermission,
        sendNotificationEmail: Boolean,
        emailMessage: String?
    ): OmhPermission {
        TODO("Not yet implemented")
    }
}
