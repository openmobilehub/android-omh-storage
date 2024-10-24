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
import com.microsoft.graph.serviceclient.GraphServiceClient
import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.storage.core.OmhStorageClient
import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageMetadata
import com.openmobilehub.android.storage.plugin.onedrive.data.mapper.DriveItemResponseToOmhStorageEntity
import com.openmobilehub.android.storage.plugin.onedrive.data.mapper.DriveItemToOmhStorageEntity
import com.openmobilehub.android.storage.plugin.onedrive.data.repository.OneDriveFileRepository
import com.openmobilehub.android.storage.plugin.onedrive.data.service.OneDriveApiClient
import com.openmobilehub.android.storage.plugin.onedrive.data.service.OneDriveApiService
import com.openmobilehub.android.storage.plugin.onedrive.data.service.OneDriveAuthProvider
import com.openmobilehub.android.storage.plugin.onedrive.data.service.retrofit.OneDriveRestApiServiceProvider
import java.io.ByteArrayOutputStream
import java.io.File

@Suppress("TooManyFunctions")
internal class OneDriveOmhStorageClient @VisibleForTesting internal constructor(
    authClient: OmhAuthClient,
    private val repository: OneDriveFileRepository
) : OmhStorageClient(authClient) {

    internal class Builder : OmhStorageClient.Builder {

        override fun build(authClient: OmhAuthClient): OmhStorageClient {
            val authProvider = OneDriveAuthProvider(authClient)
            val apiClient = OneDriveApiClient(authProvider)
            val apiService = OneDriveApiService(apiClient)
            val driveItemToOmhStorageEntity =
                DriveItemToOmhStorageEntity(MimeTypeMap.getSingleton())
            val driveItemResponseToOmhStorageEntity =
                DriveItemResponseToOmhStorageEntity(MimeTypeMap.getSingleton())

            val oneDriveRestApiServiceProvider = OneDriveRestApiServiceProvider(authClient)

            val repository = OneDriveFileRepository(
                apiService,
                oneDriveRestApiServiceProvider,
                driveItemToOmhStorageEntity,
                driveItemResponseToOmhStorageEntity
            )

            return OneDriveOmhStorageClient(authClient, repository)
        }
    }

    override val rootFolder: String
        get() = OneDriveConstants.ROOT_FOLDER

    override suspend fun listFiles(parentId: String): List<OmhStorageEntity> {
        return repository.getFilesList(parentId)
    }

    override suspend fun search(query: String): List<OmhStorageEntity> {
        return repository.search(query)
    }

    override suspend fun createFileWithMimeType(
        name: String,
        mimeType: String,
        parentId: String
    ): OmhStorageEntity? {
        throw UnsupportedOperationException(
            "OneDrive does not support creating files with mime type. Use createFileWithExtension instead."
        )
    }

    override suspend fun createFileWithExtension(
        name: String,
        extension: String,
        parentId: String
    ): OmhStorageEntity? {
        return repository.createFile(name, extension, parentId)
    }

    override suspend fun createFolder(name: String, parentId: String): OmhStorageEntity? {
        return repository.createFolder(name, parentId)
    }

    override suspend fun deleteFile(id: String) {
        return repository.deleteFile(id)
    }

    override suspend fun permanentlyDeleteFile(id: String) {
        throw UnsupportedOperationException()
    }

    override suspend fun uploadFile(localFileToUpload: File, parentId: String?): OmhStorageEntity? {
        val safeParentId = parentId ?: rootFolder
        return repository.uploadFile(localFileToUpload, safeParentId)
    }

    override suspend fun downloadFile(fileId: String): ByteArrayOutputStream {
        return repository.downloadFile(fileId)
    }

    override suspend fun exportFile(
        fileId: String,
        exportedMimeType: String
    ): ByteArrayOutputStream {
        throw UnsupportedOperationException("Exporting files is not supported in OneDrive.")
    }

    override suspend fun updateFile(
        localFileToUpload: File,
        fileId: String
    ): OmhStorageEntity {
        return repository.updateFile(localFileToUpload, fileId)
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
        return repository.getFilePermissions(fileId)
    }

    override suspend fun getFileMetadata(fileId: String): OmhStorageMetadata? {
        return repository.getFileMetadata(fileId)
    }

    override suspend fun deletePermission(fileId: String, permissionId: String) {
        return repository.deletePermission(fileId, permissionId)
    }

    override suspend fun updatePermission(
        fileId: String,
        permissionId: String,
        role: OmhPermissionRole
    ): OmhPermission {
        return repository.updatePermission(fileId, permissionId, role)
    }

    override suspend fun createPermission(
        fileId: String,
        permission: OmhCreatePermission,
        sendNotificationEmail: Boolean,
        emailMessage: String?
    ): OmhPermission {
        return repository.createPermission(fileId, permission, sendNotificationEmail, emailMessage)
    }

    override suspend fun getWebUrl(fileId: String): String? {
        return repository.getWebUrl(fileId)
    }

    override suspend fun resolvePath(path: String): OmhStorageEntity? {
        return repository.resolvePath(path)
    }

    override fun getProviderSdk(): GraphServiceClient =
        repository.apiService.apiClient.graphServiceClient

    override suspend fun getStorageUsage(): Long {
        return repository.getStorageUsage()
    }

    override suspend fun getStorageQuota(): Long {
        return repository.getStorageQuota()
    }
}
