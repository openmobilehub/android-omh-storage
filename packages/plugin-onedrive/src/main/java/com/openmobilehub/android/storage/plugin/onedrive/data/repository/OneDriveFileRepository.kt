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
import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.model.OmhStorageMetadata
import com.openmobilehub.android.storage.plugin.onedrive.data.mapper.DriveItemToOmhStorageEntity
import com.openmobilehub.android.storage.plugin.onedrive.data.mapper.toDriveRecipient
import com.openmobilehub.android.storage.plugin.onedrive.data.mapper.toOmhPermission
import com.openmobilehub.android.storage.plugin.onedrive.data.mapper.toOmhVersion
import com.openmobilehub.android.storage.plugin.onedrive.data.mapper.toOneDriveString
import com.openmobilehub.android.storage.plugin.onedrive.data.service.OneDriveApiService
import com.openmobilehub.android.storage.plugin.onedrive.data.util.toByteArrayOutputStream
import java.io.ByteArrayOutputStream
import java.io.File

@Suppress("TooManyFunctions")
class OneDriveFileRepository(
    private val apiService: OneDriveApiService,
    private val driveItemToOmhStorageEntity: DriveItemToOmhStorageEntity
) {
    fun getFilesList(parentId: String): List<OmhStorageEntity> {
        return apiService.getFilesList(parentId).map {
            driveItemToOmhStorageEntity(it)
        }
    }

    fun uploadFile(localFileToUpload: File, parentId: String): OmhStorageEntity? {
        val driveItem = apiService.uploadFile(localFileToUpload, parentId)

        return driveItem?.let { driveItemToOmhStorageEntity(it) }
    }

    fun downloadFile(fileId: String): ByteArrayOutputStream {
        val inputStream = apiService.downloadFile(fileId)
            ?: throw OmhStorageException.ApiException(message = "GraphServiceClient did not return InputStream")

        return inputStream.toByteArrayOutputStream()
    }

    fun getFileVersions(fileId: String): List<OmhFileVersion> {
        return apiService.getFileVersions(fileId).value.map {
            it.toOmhVersion(fileId)
        }
    }

    fun downloadFileVersion(fileId: String, versionId: String): ByteArrayOutputStream {
        val inputStream = apiService.downloadFileVersion(fileId, versionId)
            ?: throw OmhStorageException.ApiException(message = "GraphServiceClient did not return InputStream")

        return inputStream.toByteArrayOutputStream()
    }

    fun deleteFile(fileId: String): Boolean {
        apiService.deleteFile(fileId)

        // It returns true if the file was deleted successfully, otherwise the method will throw an exception
        return true
    }

    fun getFileMetadata(fileId: String): OmhStorageMetadata? {
        val driveItem = apiService.getFile(fileId) ?: return null

        return OmhStorageMetadata(driveItemToOmhStorageEntity(driveItem), driveItem)
    }

    fun getFilePermissions(fileId: String): List<OmhPermission> {
        return apiService.getFilePermissions(fileId).mapNotNull { it.toOmhPermission() }
    }

    fun getWebUrl(fileId: String): String? {
        return apiService.getFile(fileId)?.webUrl
    }

    fun createPermission(
        fileId: String,
        permission: OmhCreatePermission,
        sendNotificationEmail: Boolean,
        emailMessage: String?
    ): OmhPermission {
        val requestBody = InvitePostRequestBody().apply {
            roles = listOf(permission.role.toOneDriveString())
            recipients = listOf(permission.toDriveRecipient())
            message = emailMessage
            sendInvitation = sendNotificationEmail
        }

        return apiService.createPermission(fileId, requestBody).firstOrNull()?.toOmhPermission()
            ?: throw OmhStorageException.ApiException(
                message = "Create succeeded but API failed to return expected permission"
            )
    }

    fun deletePermission(fileId: String, permissionId: String): Boolean {
        apiService.deletePermission(fileId, permissionId)

        // It returns true if the permission was deleted successfully, otherwise the method will throw an exception
        return true
    }
}
