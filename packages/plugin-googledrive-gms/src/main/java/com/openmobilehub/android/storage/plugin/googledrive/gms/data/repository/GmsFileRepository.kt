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

package com.openmobilehub.android.storage.plugin.googledrive.gms.data.repository

import android.webkit.MimeTypeMap
import com.google.api.client.http.FileContent
import com.google.api.client.http.HttpResponseException
import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.model.OmhStorageMetadata
import com.openmobilehub.android.storage.plugin.googledrive.gms.GoogleDriveGmsConstants
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.mapper.ExceptionMapper
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.mapper.toOmhFileVersions
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.mapper.toOmhPermission
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.mapper.toOmhStorageEntities
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.mapper.toOmhStorageEntity
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.mapper.toPermission
import com.openmobilehub.android.storage.plugin.googledrive.gms.data.service.GoogleDriveApiService
import java.io.ByteArrayOutputStream
import java.io.File
import com.google.api.services.drive.model.File as GoogleDriveFile

@Suppress("TooManyFunctions")
internal class GmsFileRepository(
    internal val apiService: GoogleDriveApiService
) {
    companion object {
        private const val ANY_MIME_TYPE = "*/*"
    }

    internal interface Builder {
        fun build(authClient: OmhAuthClient): GmsFileRepository
    }

    fun getFilesList(parentId: String): List<OmhStorageEntity> = try {
        apiService.getFilesList(parentId).execute().toOmhStorageEntities()
    } catch (exception: HttpResponseException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun search(query: String): List<OmhStorageEntity> = try {
        apiService.search(query).execute().toOmhStorageEntities()
    } catch (exception: HttpResponseException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun createFile(name: String, mimeType: String, parentId: String?): OmhStorageEntity? = try {
        val fileToBeCreated = GoogleDriveFile().apply {
            this.name = name
            this.mimeType = mimeType
            if (parentId != null) {
                this.parents = listOf(parentId)
            }
        }

        val responseFile: GoogleDriveFile = apiService.createFile(fileToBeCreated).execute()

        responseFile.toOmhStorageEntity()
    } catch (exception: HttpResponseException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun createFolder(name: String, parentId: String?): OmhStorageEntity? {
        return createFile(name, GoogleDriveGmsConstants.FOLDER_MIME_TYPE, parentId)
    }

    fun deleteFile(fileId: String): Unit = try {
        val file = GoogleDriveFile().apply {
            trashed = true
        }
        apiService.updateFile(fileId, file).execute()
        Unit
    } catch (exception: HttpResponseException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun permanentlyDeleteFile(fileId: String): Unit = try {
        apiService.deleteFile(fileId).execute()
        Unit
    } catch (exception: HttpResponseException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun uploadFile(localFileToUpload: File, parentId: String?): OmhStorageEntity? = try {
        val localMimeType = getStringMimeTypeFromLocalFile(localFileToUpload)

        val file = GoogleDriveFile().apply {
            name = localFileToUpload.name
            mimeType = localMimeType
            parents = if (parentId.isNullOrBlank()) {
                emptyList()
            } else {
                listOf(parentId)
            }
        }

        val mediaContent = FileContent(localMimeType, localFileToUpload)

        val response: GoogleDriveFile = apiService.uploadFile(file, mediaContent).execute()

        response.toOmhStorageEntity()
    } catch (exception: HttpResponseException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    private fun getStringMimeTypeFromLocalFile(file: File) = MimeTypeMap
        .getSingleton()
        .getMimeTypeFromExtension(file.extension)
        ?: ANY_MIME_TYPE

    fun downloadFile(fileId: String): ByteArrayOutputStream = try {
        val outputStream = ByteArrayOutputStream()

        val file = apiService.getFile(fileId).execute()

        // executeMediaAndDownloadTo does not handle empty files correctly
        if (file.getSize() == 0L) {
            apiService.getFile(fileId).executeMediaAsInputStream().use {
                it.copyTo(outputStream)
            }
        } else {
            apiService.getFile(fileId).executeMediaAndDownloadTo(outputStream)
        }

        outputStream
    } catch (exception: HttpResponseException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun exportFile(fileId: String, exportedMimeType: String): ByteArrayOutputStream = try {
        val outputStream = ByteArrayOutputStream()

        apiService.exportFile(fileId, exportedMimeType)
            .executeMediaAndDownloadTo(outputStream)
        outputStream
    } catch (exception: HttpResponseException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun updateFile(localFileToUpload: File, fileId: String): OmhStorageEntity.OmhFile? = try {
        val localMimeType = getStringMimeTypeFromLocalFile(localFileToUpload)

        val file = GoogleDriveFile().apply {
            name = localFileToUpload.name
            mimeType = localMimeType
        }

        val mediaContent = FileContent(localMimeType, localFileToUpload)

        val response: GoogleDriveFile =
            apiService.updateFile(fileId, file, mediaContent).execute()

        response.toOmhStorageEntity() as? OmhStorageEntity.OmhFile
    } catch (exception: HttpResponseException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun getFileVersions(fileId: String): List<OmhFileVersion> = try {
        apiService.getFileRevisions(fileId).execute().toOmhFileVersions(fileId).reversed()
    } catch (exception: HttpResponseException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun downloadFileVersion(fileId: String, versionId: String): ByteArrayOutputStream = try {
        val outputStream = ByteArrayOutputStream()
        apiService.downloadFileRevision(fileId, versionId).executeMediaAndDownloadTo(outputStream)

        outputStream
    } catch (exception: HttpResponseException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun getFileMetadata(fileId: String): OmhStorageMetadata? {
        try {
            val file = apiService.getFileMetadata(fileId).execute()

            return OmhStorageMetadata(file.toOmhStorageEntity() ?: return null, file)
        } catch (exception: HttpResponseException) {
            throw ExceptionMapper.toOmhApiException(exception)
        }
    }

    fun getFilePermissions(fileId: String): List<OmhPermission> = try {
        val file = apiService
            .getPermission(fileId)
            .execute()
        val permissions = file
            .permissions

        permissions?.mapNotNull { it.toOmhPermission() } ?: emptyList()
    } catch (exception: HttpResponseException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun deletePermission(fileId: String, permissionId: String): Unit = try {
        apiService.deletePermission(fileId, permissionId).execute()
        Unit
    } catch (exception: HttpResponseException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun updatePermission(
        fileId: String,
        permissionId: String,
        role: OmhPermissionRole
    ): OmhPermission = try {
        val transferOwnership = role == OmhPermissionRole.OWNER

        val result =
            apiService.updatePermission(
                fileId,
                permissionId,
                role.toPermission(),
                transferOwnership
            ).execute()
        result.toOmhPermission()
            ?: throw OmhStorageException.ApiException(
                message = "Update succeeded but API failed to return expected permission"
            )
    } catch (exception: HttpResponseException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun createPermission(
        fileId: String,
        omhCreatePermission: OmhCreatePermission,
        sendNotificationEmail: Boolean,
        emailMessage: String?
    ): OmhPermission = try {
        val transferOwnership = omhCreatePermission.role == OmhPermissionRole.OWNER
        val willSendNotificationEmail = sendNotificationEmail || transferOwnership

        val result =
            apiService.createPermission(
                fileId,
                omhCreatePermission.toPermission(),
                transferOwnership,
                willSendNotificationEmail,
                emailMessage
            )
                .execute()
        result.toOmhPermission()
            ?: throw OmhStorageException.ApiException(
                message = "Create succeeded but API failed to return expected permission"
            )
    } catch (exception: HttpResponseException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun getWebUrl(fileId: String): String? = try {
        apiService.getWebUrl(fileId).execute()?.webViewLink
    } catch (exception: HttpResponseException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun getStorageUsage(): Long = try {
        apiService.about().execute().storageQuota.usageInDrive
    } catch (exception: HttpResponseException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun getStorageQuota(): Long = try {
        val retval: Long? = apiService.about().execute().storageQuota.limit
        retval ?: -1L
    } catch (exception: HttpResponseException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun resolvePath(path: String): OmhStorageEntity? {
        val nodeId = apiService.queryNodeIdHaving(path)
        return nodeId?.let {
            apiService.getFile(it).execute().toOmhStorageEntity()
        }
    }
}
