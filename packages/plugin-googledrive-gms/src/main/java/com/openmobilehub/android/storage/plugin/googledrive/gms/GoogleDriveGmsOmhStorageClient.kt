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

package com.openmobilehub.android.storage.plugin.googledrive.gms

import com.google.api.services.drive.Drive
import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.auth.plugin.google.gms.GmsCredentials
import com.openmobilehub.android.storage.core.OmhStorageClient
import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.model.OmhStorageMetadata
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.repository.GmsFileRepository
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.service.GoogleDriveApiProvider
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.service.GoogleDriveApiService
import java.io.ByteArrayOutputStream
import java.io.File

@Suppress("TooManyFunctions")
internal class GoogleDriveGmsOmhStorageClient private constructor(
    authClient: OmhAuthClient,
    private val repositoryBuilder: RepositoryBuilder,
) : OmhStorageClient(authClient) {

    internal class Builder : OmhStorageClient.Builder {

        override fun build(authClient: OmhAuthClient): OmhStorageClient {
            return GoogleDriveGmsOmhStorageClient(authClient, RepositoryBuilder())
        }
    }

    private class RepositoryBuilder : GmsFileRepository.Builder {
        override fun build(authClient: OmhAuthClient): GmsFileRepository {
            val credentials =
                (authClient.getCredentials() as? GmsCredentials)?.googleAccountCredential
                    ?: throw OmhStorageException.InvalidCredentialsException(
                        "Couldn't get access token from auth client"
                    )

            val apiProvider = GoogleDriveApiProvider.getInstance(credentials)
            val apiService = GoogleDriveApiService(apiProvider)
            return GmsFileRepository(apiService)
        }
    }

    private val fileRepository: GmsFileRepository
        get() = repositoryBuilder.build(authClient)

    override val rootFolder: String
        get() = GoogleDriveGmsConstants.ROOT_FOLDER

    override suspend fun listFiles(parentId: String): List<OmhStorageEntity> {
        return fileRepository.getFilesList(parentId)
    }

    override suspend fun search(query: String): List<OmhStorageEntity> {
        return fileRepository.search(query)
    }

    override suspend fun createFileWithMimeType(
        name: String,
        mimeType: String,
        parentId: String
    ): OmhStorageEntity? {
        return fileRepository.createFile(name, mimeType, parentId)
    }

    override suspend fun createFileWithExtension(
        name: String,
        extension: String,
        parentId: String
    ): OmhStorageEntity? {
        throw UnsupportedOperationException(
            "Google Drive does not support creating files with extensions. Use createFileWithMimeType instead."
        )
    }

    override suspend fun createFolder(
        name: String,
        parentId: String
    ): OmhStorageEntity? {
        return fileRepository.createFolder(name, parentId)
    }

    override suspend fun deleteFile(id: String) {
        fileRepository.deleteFile(id)
    }

    override suspend fun permanentlyDeleteFile(id: String) {
        fileRepository.permanentlyDeleteFile(id)
    }

    override suspend fun uploadFile(localFileToUpload: File, parentId: String?): OmhStorageEntity? {
        return fileRepository.uploadFile(localFileToUpload, parentId)
    }

    override suspend fun downloadFile(fileId: String): ByteArrayOutputStream {
        return fileRepository.downloadFile(fileId)
    }

    override suspend fun exportFile(
        fileId: String,
        exportedMimeType: String
    ): ByteArrayOutputStream {
        return fileRepository.exportFile(fileId, exportedMimeType)
    }

    override suspend fun updateFile(
        localFileToUpload: File,
        fileId: String
    ): OmhStorageEntity.OmhFile? {
        return fileRepository.updateFile(localFileToUpload, fileId)
    }

    override suspend fun getFileVersions(fileId: String): List<OmhFileVersion> {
        return fileRepository.getFileVersions(fileId)
    }

    override suspend fun downloadFileVersion(
        fileId: String,
        versionId: String
    ): ByteArrayOutputStream {
        return fileRepository.downloadFileVersion(fileId, versionId)
    }

    override suspend fun getFilePermissions(fileId: String): List<OmhPermission> {
        return fileRepository.getFilePermissions(fileId)
    }

    override suspend fun deletePermission(fileId: String, permissionId: String) {
        return fileRepository.deletePermission(fileId, permissionId)
    }

    override suspend fun updatePermission(
        fileId: String,
        permissionId: String,
        role: OmhPermissionRole
    ): OmhPermission {
        return fileRepository.updatePermission(fileId, permissionId, role)
    }

    override suspend fun createPermission(
        fileId: String,
        permission: OmhCreatePermission,
        sendNotificationEmail: Boolean,
        emailMessage: String?
    ): OmhPermission {
        return fileRepository.createPermission(
            fileId,
            permission,
            sendNotificationEmail,
            emailMessage
        )
    }

    override suspend fun getFileMetadata(fileId: String): OmhStorageMetadata? {
        return fileRepository.getFileMetadata(fileId)
    }

    override suspend fun getWebUrl(fileId: String): String? {
        return fileRepository.getWebUrl(fileId)
    }

    override suspend fun resolvePath(path: String): OmhStorageEntity? {
        return fileRepository.resolvePath(path)
    }

    override fun getProviderSdk(): Drive = fileRepository.apiService.apiProvider.googleDriveApiService

    override suspend fun getStorageUsage(): Long {
        return fileRepository.getStorageUsage()
    }

    override suspend fun getStorageQuota(): Long {
        return fileRepository.getStorageQuota()
    }
}
