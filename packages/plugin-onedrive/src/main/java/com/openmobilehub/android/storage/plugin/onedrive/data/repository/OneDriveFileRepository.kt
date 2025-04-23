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

package com.openmobilehub.android.storage.plugin.onedrive.data.repository

import com.microsoft.graph.drives.item.items.item.invite.InvitePostRequestBody
import com.microsoft.graph.models.DriveItem
import com.microsoft.kiota.ApiException
import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.model.OmhStorageMetadata
import com.openmobilehub.android.storage.plugin.onedrive.data.mapper.DriveItemResponseToOmhStorageEntity
import com.openmobilehub.android.storage.plugin.onedrive.data.mapper.DriveItemToOmhStorageEntity
import com.openmobilehub.android.storage.plugin.onedrive.data.mapper.ExceptionMapper
import com.openmobilehub.android.storage.plugin.onedrive.data.mapper.toDriveRecipient
import com.openmobilehub.android.storage.plugin.onedrive.data.mapper.toOmhPermission
import com.openmobilehub.android.storage.plugin.onedrive.data.mapper.toOmhVersion
import com.openmobilehub.android.storage.plugin.onedrive.data.mapper.toOneDriveString
import com.openmobilehub.android.storage.plugin.onedrive.data.service.OneDriveApiService
import com.openmobilehub.android.storage.plugin.onedrive.data.service.retrofit.OneDriveRestApiServiceProvider
import com.openmobilehub.android.storage.plugin.onedrive.data.service.retrofit.body.CreateFolderRequestBody
import com.openmobilehub.android.storage.plugin.onedrive.data.util.toApiException
import com.openmobilehub.android.storage.plugin.onedrive.data.util.toByteArrayOutputStream
import java.io.ByteArrayOutputStream
import java.io.File

@Suppress("TooManyFunctions")
internal class OneDriveFileRepository(
    internal val apiService: OneDriveApiService,
    private val retrofitImpl: OneDriveRestApiServiceProvider,
    private val driveItemToOmhStorageEntity: DriveItemToOmhStorageEntity,
    private val driveItemResponseToOmhStorageEntity: DriveItemResponseToOmhStorageEntity
) {

    companion object {
        private const val PRECONDITION_ERROR_STATUS_CODE = 412
        private const val SMALL_FILE_SIZE = 1024 * 1024 // 1MB
    }

    fun getFilesList(parentId: String): List<OmhStorageEntity> = try {
        apiService.getFilesList(parentId).map {
            driveItemToOmhStorageEntity(it)
        }
    } catch (exception: ApiException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun uploadFile(localFileToUpload: File, parentId: String): OmhStorageEntity? = try {
        val path = "$parentId:/${localFileToUpload.name}:"

        val driveItem = if (localFileToUpload.length() < SMALL_FILE_SIZE) {
            apiService.uploadFile(localFileToUpload, path)
            apiService.getFile(path)
        } else {
            apiService.resumableUploadFile(localFileToUpload, path)
        }

        driveItem?.let { driveItemToOmhStorageEntity(it) }
    } catch (exception: ApiException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun downloadFile(fileId: String): ByteArrayOutputStream = try {
        val inputStream = apiService.downloadFile(fileId)
            ?: throw OmhStorageException.ApiException(message = "GraphServiceClient did not return InputStream")

        inputStream.toByteArrayOutputStream()
    } catch (exception: ApiException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun getFileVersions(fileId: String): List<OmhFileVersion> = try {
        apiService.getFileVersions(fileId).value.map {
            it.toOmhVersion(fileId)
        }
    } catch (exception: ApiException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun downloadFileVersion(fileId: String, versionId: String): ByteArrayOutputStream = try {
        val inputStream = apiService.downloadFileVersion(fileId, versionId)
            ?: throw OmhStorageException.ApiException(message = "GraphServiceClient did not return InputStream")

        inputStream.toByteArrayOutputStream()
    } catch (exception: ApiException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun deleteFile(fileId: String) = try {
        apiService.deleteFile(fileId)
    } catch (exception: ApiException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun getFileMetadata(fileId: String): OmhStorageMetadata? {
        try {
            val driveItem = apiService.getFile(fileId) ?: return null

            return OmhStorageMetadata(driveItemToOmhStorageEntity(driveItem), driveItem)
        } catch (exception: ApiException) {
            throw ExceptionMapper.toOmhApiException(exception)
        }
    }

    fun getFilePermissions(fileId: String): List<OmhPermission> = try {
        apiService.getFilePermissions(fileId).mapNotNull { it.toOmhPermission() }
    } catch (exception: ApiException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun getWebUrl(fileId: String): String? = try {
        apiService.getFile(fileId)?.webUrl
    } catch (exception: ApiException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun createPermission(
        fileId: String,
        permission: OmhCreatePermission,
        sendNotificationEmail: Boolean,
        emailMessage: String?
    ): OmhPermission = try {
        val requestBody = InvitePostRequestBody().apply {
            roles = listOf(permission.role.toOneDriveString())
            recipients = listOf(permission.toDriveRecipient())
            message = emailMessage
            sendInvitation = sendNotificationEmail
            requireSignIn = true
        }

        apiService.createPermission(fileId, requestBody).firstOrNull()?.toOmhPermission()
            ?: throw OmhStorageException.ApiException(
                message = "Create succeeded but API failed to return expected permission"
            )
    } catch (exception: ApiException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun deletePermission(fileId: String, permissionId: String) = try {
        apiService.deletePermission(fileId, permissionId)
    } catch (exception: ApiException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun updatePermission(
        fileId: String,
        permissionId: String,
        role: OmhPermissionRole
    ): OmhPermission = try {
        apiService
            .updatePermission(fileId, permissionId, role.toOneDriveString()).toOmhPermission()
            ?: throw OmhStorageException.ApiException(
                message = "Update succeeded but API failed to return expected permission"
            )
    } catch (exception: ApiException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    suspend fun createFolder(name: String, parentId: String): OmhStorageEntity {
        val driveId = apiService.driveId

        val response = retrofitImpl.getOneDriveApiService().createFolder(
            driveId = driveId,
            parentId = parentId,
            body = CreateFolderRequestBody(name),
        )

        return if (response.isSuccessful) {
            response.body()?.let { driveItemResponseToOmhStorageEntity(it) }
                ?: throw OmhStorageException.ApiException(
                    message = "Create succeeded but API failed to return expected folder"
                )
        } else {
            throw response.toApiException()
        }
    }

    fun createFile(name: String, extension: String, parentId: String): OmhStorageEntity {
        val tempFile = File.createTempFile("tempFile", extension)

        try {
            val fullFileName = "$name.$extension"
            val path = "$parentId:/$fullFileName:"

            apiService.uploadFile(tempFile, path)

            val driveItem = apiService.getFile(path)

            return driveItem?.let { driveItemToOmhStorageEntity(it) }
                ?: throw OmhStorageException.ApiException(
                    message = "Create succeeded but API failed to return expected file"
                )
        } catch (exception: ApiException) {
            throw ExceptionMapper.toOmhApiException(exception)
        } finally {
            tempFile.delete()
        }
    }

    private fun renameFile(fileId: String, fileName: String, retry: Boolean): OmhStorageEntity {
        try {
            val updatedDriveItem = DriveItem().apply {
                name = fileName
            }
            val response = apiService.updateFileMetadata(fileId, updatedDriveItem)
            return driveItemToOmhStorageEntity(response)
        } catch (exception: ApiException) {
            // This is a workaround for the OneDrive API issue where the file name is not updated
            // when uploading a new file version. The reason is related to the race condition when
            // uploading a new file version and renaming the file at the same time.
            if (exception.responseStatusCode == PRECONDITION_ERROR_STATUS_CODE && retry) {
                return renameFile(fileId, fileName, false)
            } else {
                throw ExceptionMapper.toOmhApiException(exception)
            }
        }
    }

    fun updateFile(localFileToUpload: File, fileId: String): OmhStorageEntity {
        try {
            if (localFileToUpload.length() < SMALL_FILE_SIZE) {
                apiService.uploadFile(localFileToUpload, fileId, "replace")
                apiService.getFile(fileId)
            } else {
                apiService.resumableUploadFile(localFileToUpload, fileId, "replace")
            }
            // By default, the file name is not updated when uploading a new file version.
            return renameFile(fileId, localFileToUpload.name, true)
        } catch (exception: ApiException) {
            throw ExceptionMapper.toOmhApiException(exception)
        }
    }

    fun search(query: String): List<OmhStorageEntity> = try {
        apiService.search(query).value.map {
            driveItemToOmhStorageEntity(it)
        }
    } catch (exception: ApiException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun getStorageUsage(): Long = try {
        apiService.getDrive().quota.used ?: -1L
    } catch (exception: ApiException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun getStorageQuota(): Long = try {
        apiService.getDrive().quota.total ?: -1L
    } catch (exception: ApiException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun resolvePath(path: String): OmhStorageEntity? = kotlin.runCatching {
        apiService.resolvePath(path)?.let {
            driveItemToOmhStorageEntity(it)
        }
    }.onFailure { exception: Throwable ->
        throw ExceptionMapper.toOmhApiException(exception as ApiException)
    }.getOrNull()
}
