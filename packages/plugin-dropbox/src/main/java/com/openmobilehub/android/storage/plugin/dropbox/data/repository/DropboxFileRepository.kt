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

package com.openmobilehub.android.storage.plugin.dropbox.data.repository

import androidx.annotation.VisibleForTesting
import com.dropbox.core.DbxApiException
import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.model.OmhStorageMetadata
import com.openmobilehub.android.storage.core.utils.toInputStream
import com.openmobilehub.android.storage.plugin.dropbox.DropboxConstants
import com.openmobilehub.android.storage.plugin.dropbox.data.mapper.ExceptionMapper
import com.openmobilehub.android.storage.plugin.dropbox.data.mapper.MetadataToOmhStorageEntity
import com.openmobilehub.android.storage.plugin.dropbox.data.mapper.toAccessLevel
import com.openmobilehub.android.storage.plugin.dropbox.data.mapper.toMemberSelector
import com.openmobilehub.android.storage.plugin.dropbox.data.mapper.toOmhPermission
import com.openmobilehub.android.storage.plugin.dropbox.data.mapper.toOmhStorageEntity
import com.openmobilehub.android.storage.plugin.dropbox.data.mapper.toOmhVersion
import com.openmobilehub.android.storage.plugin.dropbox.data.service.DropboxApiService
import java.io.ByteArrayOutputStream
import java.io.File

@SuppressWarnings("TooManyFunctions")
internal class DropboxFileRepository(
    private val apiService: DropboxApiService,
    private val metadataToOmhStorageEntity: MetadataToOmhStorageEntity
) {
    internal interface Builder {
        fun build(authClient: OmhAuthClient): DropboxFileRepository
    }

    fun getFilesList(parentId: String): List<OmhStorageEntity> = try {
        val dropboxFiles = apiService.getFilesList(parentId)
        dropboxFiles.entries.mapNotNull {
            metadataToOmhStorageEntity(it)
        }
    } catch (exception: DbxApiException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun uploadFile(localFileToUpload: File, parentId: String): OmhStorageEntity? = try {
        val inputStream = localFileToUpload.toInputStream()

        val path = "$parentId/${localFileToUpload.name}"

        val response = apiService.uploadFile(inputStream, path)

        metadataToOmhStorageEntity(response)
    } catch (exception: DbxApiException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun downloadFile(fileId: String): ByteArrayOutputStream = try {
        val outputStream = ByteArrayOutputStream()
        apiService.downloadFile(fileId, outputStream)

        outputStream
    } catch (exception: DbxApiException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun getFileVersions(fileId: String): List<OmhFileVersion> = try {
        val revisions = apiService.getFileRevisions(fileId)

        revisions.entries.map {
            it.toOmhVersion()
        }
    } catch (exception: DbxApiException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun downloadFileVersion(versionId: String): ByteArrayOutputStream = try {
        val outputStream = ByteArrayOutputStream()
        apiService.downloadFileRevision(versionId, outputStream)

        outputStream
    } catch (exception: DbxApiException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun deleteFile(fileId: String): Unit = try {
        apiService.deleteFile(fileId)
        Unit
    } catch (exception: DbxApiException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun search(query: String): List<OmhStorageEntity> = try {
        val searchResults = apiService.search(query)
        searchResults.matches.mapNotNull {
            metadataToOmhStorageEntity(it.metadata.metadataValue)
        }
    } catch (exception: DbxApiException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun getFileMetadata(fileId: String): OmhStorageMetadata = try {
        val metadata = apiService.getFile(fileId)
        val omhStorageEntity = metadataToOmhStorageEntity(metadata)
            ?: throw OmhStorageException.ApiException(message = "Failed to get metadata for file with ID: $fileId")

        OmhStorageMetadata(omhStorageEntity, metadata)
    } catch (exception: DbxApiException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    @VisibleForTesting
    fun getNewFolderPath(parentId: String, name: String): String {
        if (parentId == DropboxConstants.ROOT_FOLDER) {
            return "/$name"
        } else {
            val metadata = apiService.getFile(parentId)

            metadata.pathLower?.let { return "$it/$name" }
                ?: throw OmhStorageException.ApiException(
                    message = "Failed to get path for parent folder with ID: $parentId"
                )
        }
    }

    fun createFolder(name: String, parentId: String): OmhStorageEntity? = try {
        val path = getNewFolderPath(parentId, name)
        val createFolderResult = apiService.createFolder(path)

        createFolderResult.metadata.toOmhStorageEntity()
    } catch (exception: DbxApiException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun createFileWithExtension(
        name: String,
        extension: String,
        parentId: String
    ): OmhStorageEntity? {
        val tempFile = File.createTempFile("tempFile", extension)

        try {
            val inputStream = tempFile.toInputStream()

            val fullFileName = "$name.$extension"
            val path = "$parentId/$fullFileName"

            val response = apiService.uploadFile(inputStream, path)

            return metadataToOmhStorageEntity(response)
        } catch (exception: DbxApiException) {
            throw ExceptionMapper.toOmhApiException(exception)
        } finally {
            tempFile.delete()
        }
    }

    fun createPermission(
        fileId: String,
        permission: OmhCreatePermission,
        sendNotificationEmail: Boolean,
        emailMessage: String?
    ): OmhPermission? {
        try {
            val result = apiService.createPermission(
                fileId,
                permission.toMemberSelector(),
                permission.toAccessLevel(),
                sendNotificationEmail,
                emailMessage
            ).firstOrNull()

            if (result?.result?.isSuccess == true) {
                // Dropbox does not return created permission as a result
                return null
            }

            val errorMessage = if (result?.result?.isMemberError == true) {
                result.result.memberErrorValue.toStringMultiline()
            } else {
                "Unknown error"
            }

            throw OmhStorageException.ApiException(message = errorMessage)
        } catch (exception: DbxApiException) {
            throw ExceptionMapper.toOmhApiException(exception)
        }
    }

    fun getWebUrl(fileId: String): String = try {
        apiService.getWebUrl(fileId)
    } catch (exception: DbxApiException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun getFilePermissions(fileId: String): List<OmhPermission> {
        val result = apiService.getFilePermissions(fileId)

        return result.users.mapNotNull { it.toOmhPermission() }.plus(
            result.groups.mapNotNull { it.toOmhPermission() }
        )
    }
}
