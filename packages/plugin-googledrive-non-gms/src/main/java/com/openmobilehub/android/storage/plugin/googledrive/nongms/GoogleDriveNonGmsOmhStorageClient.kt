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

package com.openmobilehub.android.storage.plugin.googledrive.nongms

import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.auth.core.OmhCredentials
import com.openmobilehub.android.storage.core.OmhStorageClient
import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.model.OmhStorageMetadata
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.NonGmsFileRepository
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.retrofit.GoogleStorageApiServiceProvider
import java.io.ByteArrayOutputStream
import java.io.File

@Suppress("TooManyFunctions")
internal class GoogleDriveNonGmsOmhStorageClient private constructor(
    authClient: OmhAuthClient,
    private val fileRepository: NonGmsFileRepository
) : OmhStorageClient(authClient) {

    internal class Builder : OmhStorageClient.Builder {

        override fun build(authClient: OmhAuthClient): OmhStorageClient {
            val omhCredentials = authClient.getCredentials() as? OmhCredentials
                ?: throw OmhStorageException.InvalidCredentialsException()

            val retrofitImpl = GoogleStorageApiServiceProvider.getInstance(omhCredentials)

            val fileRepository = NonGmsFileRepository(retrofitImpl)

            return GoogleDriveNonGmsOmhStorageClient(authClient, fileRepository)
        }
    }

    override val rootFolder: String
        get() = GoogleDriveNonGmsConstants.ROOT_FOLDER

    override suspend fun listFiles(parentId: String): List<OmhStorageEntity> {
        return fileRepository.getFilesList(parentId)
    }

    override suspend fun search(query: String): List<OmhStorageEntity> {
        return fileRepository.search(query)
    }

    override suspend fun createFile(
        name: String,
        mimeType: String,
        parentId: String
    ): OmhStorageEntity? {
        return fileRepository.createFile(name, mimeType, parentId)
    }

    override suspend fun deleteFile(id: String): Boolean {
        return fileRepository.deleteFile(id)
    }

    override suspend fun permanentlyDeleteFile(id: String): Boolean {
        return fileRepository.permanentlyDeleteFile(id)
    }

    override suspend fun uploadFile(localFileToUpload: File, parentId: String?): OmhStorageEntity? {
        return fileRepository.uploadFile(localFileToUpload, parentId)
    }

    override suspend fun downloadFile(fileId: String, mimeType: String?): ByteArrayOutputStream {
        return fileRepository.downloadFile(fileId, mimeType)
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

    override suspend fun deletePermission(fileId: String, permissionId: String): Boolean {
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
        return fileRepository.createPermission(fileId, permission, sendNotificationEmail, emailMessage)
    }

    override suspend fun getWebUrl(fileId: String): String? {
        return fileRepository.getWebUrl(fileId)
    }

    override suspend fun getFileMetadata(fileId: String): OmhStorageMetadata {
        return fileRepository.getFileMetadata(fileId)
    }
}
