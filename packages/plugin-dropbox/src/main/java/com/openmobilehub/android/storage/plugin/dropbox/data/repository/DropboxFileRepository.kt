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
import com.dropbox.core.DbxException
import com.dropbox.core.v2.files.FolderMetadata
import com.dropbox.core.v2.files.WriteMode
import com.dropbox.core.v2.sharing.SharedFolderMetadata
import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.model.OmhStorageMetadata
import com.openmobilehub.android.storage.core.utils.toInputStream
import com.openmobilehub.android.storage.plugin.dropbox.DropboxConstants
import com.openmobilehub.android.storage.plugin.dropbox.data.mapper.ExceptionMapper
import com.openmobilehub.android.storage.plugin.dropbox.data.mapper.MetadataToOmhStorageEntity
import com.openmobilehub.android.storage.plugin.dropbox.data.mapper.toAccessLevel
import com.openmobilehub.android.storage.plugin.dropbox.data.mapper.toAddMember
import com.openmobilehub.android.storage.plugin.dropbox.data.mapper.toMemberSelector
import com.openmobilehub.android.storage.plugin.dropbox.data.mapper.toOmhPermission
import com.openmobilehub.android.storage.plugin.dropbox.data.mapper.toOmhStorageEntity
import com.openmobilehub.android.storage.plugin.dropbox.data.mapper.toOmhVersion
import com.openmobilehub.android.storage.plugin.dropbox.data.service.DropboxApiService
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream
import java.io.File

@SuppressWarnings("TooManyFunctions")
internal class DropboxFileRepository(
    internal val apiService: DropboxApiService,
    private val metadataToOmhStorageEntity: MetadataToOmhStorageEntity
) {

    companion object {
        const val CHECK_JOB_STATUS_RETRY_TIMES = 10
        const val CHECK_JOB_STATUS_DELAY = 1_000L // one sec
    }

    internal interface Builder {
        fun build(authClient: OmhAuthClient): DropboxFileRepository
    }

    fun getFilesList(parentId: String): List<OmhStorageEntity> = try {
        val dropboxFiles = apiService.getFilesList(parentId)
        dropboxFiles.entries.mapNotNull {
            metadataToOmhStorageEntity(it)
        }
    } catch (exception: DbxException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun uploadFile(localFileToUpload: File, parentId: String): OmhStorageEntity? = try {
        val inputStream = localFileToUpload.toInputStream()

        val path = "$parentId/${localFileToUpload.name}"

        val response = apiService.uploadFile(inputStream, path)

        metadataToOmhStorageEntity(response)
    } catch (exception: DbxException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun downloadFile(fileId: String): ByteArrayOutputStream = try {
        val outputStream = ByteArrayOutputStream()
        apiService.downloadFile(fileId, outputStream)

        outputStream
    } catch (exception: DbxException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun getFileVersions(fileId: String): List<OmhFileVersion> = try {
        val revisions = apiService.getFileRevisions(fileId)

        revisions.entries.map {
            it.toOmhVersion()
        }
    } catch (exception: DbxException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun downloadFileVersion(versionId: String): ByteArrayOutputStream = try {
        val outputStream = ByteArrayOutputStream()
        apiService.downloadFileRevision(versionId, outputStream)

        outputStream
    } catch (exception: DbxException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun deleteFile(fileId: String): Unit = try {
        apiService.deleteFile(fileId)
        Unit
    } catch (exception: DbxException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun search(query: String): List<OmhStorageEntity> = try {
        val searchResults = apiService.search(query)
        searchResults.matches.mapNotNull {
            metadataToOmhStorageEntity(it.metadata.metadataValue)
        }
    } catch (exception: DbxException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun getFileMetadata(fileId: String): OmhStorageMetadata = try {
        val metadata = apiService.getFile(fileId)
        val omhStorageEntity = metadataToOmhStorageEntity(metadata)
            ?: throw OmhStorageException.ApiException(message = "Failed to get metadata for file with ID: $fileId")

        OmhStorageMetadata(omhStorageEntity, metadata)
    } catch (exception: DbxException) {
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
    } catch (exception: DbxException) {
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
        } catch (exception: DbxException) {
            throw ExceptionMapper.toOmhApiException(exception)
        } finally {
            tempFile.delete()
        }
    }

    fun renameFile(fileId: String, newName: String): OmhStorageEntity? {
        try {
            val fileMetadata = apiService.getFile(fileId)

            if (fileMetadata.name == newName) {
                return metadataToOmhStorageEntity(fileMetadata)
            }

            val pathLower = fileMetadata.pathLower
                ?: throw OmhStorageException.ApiException(
                    message = "Failed to get path for file with ID: $fileId"
                )

            val pathWithoutFileName =
                pathLower.substringBeforeLast(fileMetadata.name.lowercase())
            val newPath = "$pathWithoutFileName$newName"

            val result = apiService.moveFile(pathLower, newPath)

            return metadataToOmhStorageEntity(result.metadata)
        } catch (exception: DbxException) {
            throw ExceptionMapper.toOmhApiException(exception)
        }
    }

    fun updateFile(newFile: File, fileId: String): OmhStorageEntity? = try {
        val inputStream = newFile.toInputStream()

        val fileMetadata = apiService.getFile(fileId)

        val pathLower = fileMetadata.pathLower
            ?: throw OmhStorageException.ApiException(
                message = "Failed to get path for file with ID: $fileId"
            )

        apiService.uploadFile(
            inputStream,
            pathLower,
            false,
            WriteMode.OVERWRITE
        )

        renameFile(fileId, newFile.name)
    } catch (exception: DbxException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    suspend fun createPermission(
        fileId: String,
        permission: OmhCreatePermission,
        sendNotificationEmail: Boolean,
        emailMessage: String?
    ) = try {
        val folderMetadata = isFolder(fileId)

        if (folderMetadata != null) {
            val sharedFolderId = folderMetadata.sharedFolderId

            if (sharedFolderId == null) {
                shareFolder(
                    fileId,
                    permission,
                    sendNotificationEmail,
                    emailMessage
                )
            } else {
                createFolderPermission(
                    sharedFolderId,
                    permission,
                    sendNotificationEmail,
                    emailMessage
                )
            }
        } else {
            createFilePermission(fileId, permission, sendNotificationEmail, emailMessage)
        }
    } catch (exception: DbxException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    private fun createFilePermission(
        fileId: String,
        permission: OmhCreatePermission,
        sendNotificationEmail: Boolean,
        emailMessage: String?
    ) {
        val result = apiService.createFilePermission(
            fileId,
            permission.toMemberSelector(),
            permission.toAccessLevel(),
            sendNotificationEmail,
            emailMessage
        ).firstOrNull()

        if (result?.result?.isSuccess != true) {
            val errorMessage = if (result?.result?.isMemberError == true) {
                result.result.memberErrorValue.toStringMultiline()
            } else {
                "Unknown error"
            }

            throw OmhStorageException.ApiException(message = errorMessage)
        }
    }

    private suspend fun shareFolder(
        folderId: String,
        permission: OmhCreatePermission,
        sendNotificationEmail: Boolean,
        emailMessage: String?
    ) {
        val shareFolderLaunch = apiService.shareFolder(
            folderId,
        )

        val sharedFolderId = if (!shareFolderLaunch.isComplete) {
            waitForShareJobToFinish(shareFolderLaunch.asyncJobIdValue)
        } else {
            shareFolderLaunch.completeValue
        }.sharedFolderId

        createFolderPermission(
            sharedFolderId,
            permission,
            sendNotificationEmail,
            emailMessage
        )
    }

    private suspend fun waitForShareJobToFinish(
        asyncJobIdValue: String
    ): SharedFolderMetadata {
        repeat(CHECK_JOB_STATUS_RETRY_TIMES) {
            val result = apiService.checkShareFolderJobStatus(asyncJobIdValue)
            if (result.isComplete) {
                return result.completeValue
            }
            delay(CHECK_JOB_STATUS_DELAY)
        }

        throw OmhStorageException.ApiException(message = "Sharing folder job did not finish in expected time")
    }

    private fun createFolderPermission(
        sharedFolderId: String,
        permission: OmhCreatePermission,
        sendNotificationEmail: Boolean,
        emailMessage: String?
    ) {
        apiService.createFolderPermission(
            sharedFolderId,
            permission.toAddMember(),
            sendNotificationEmail,
            emailMessage
        )
    }

    fun getWebUrl(fileId: String): String? {
        try {
            val folderMetadata = isFolder(fileId)

            return if (folderMetadata != null) {
                apiService.getFolderWebUrl(
                    folderMetadata.sharedFolderId ?: return null
                )
            } else {
                apiService.getFileWebUrl(fileId)
            }
        } catch (exception: DbxException) {
            throw ExceptionMapper.toOmhApiException(exception)
        }
    }

    fun deletePermission(fileId: String, permissionId: String) {
        try {
            val folderMetadata = isFolder(fileId)
            val memberSelector = permissionId.toMemberSelector()

            if (folderMetadata != null) {
                apiService.deleteFolderPermission(
                    folderMetadata.sharedFolderId
                        ?: throw OmhStorageException.ApiException(message = "This is not a shared folder"),
                    memberSelector
                )
            } else {
                apiService.deleteFilePermission(fileId, memberSelector)
            }
        } catch (exception: DbxException) {
            throw ExceptionMapper.toOmhApiException(exception)
        }
    }

    fun updatePermission(
        fileId: String,
        permissionId: String,
        role: OmhPermissionRole
    ) = try {
        val folderMetadata = isFolder(fileId)
        val memberSelector = permissionId.toMemberSelector()

        if (folderMetadata != null) {
            apiService.updateFolderPermissions(
                folderMetadata.sharedFolderId
                    ?: throw OmhStorageException.ApiException(message = "This is not a shared folder"),
                memberSelector,
                role.toAccessLevel()
            )
        } else {
            apiService.updateFilePermissions(fileId, memberSelector, role.toAccessLevel())
        }
    } catch (exception: DbxException) {
        throw ExceptionMapper.toOmhApiException(exception)
    }

    fun getPermissions(fileId: String): List<OmhPermission> {
        try {
            val folderMetadata = isFolder(fileId)

            return if (folderMetadata != null) {
                getFolderPermissions(folderMetadata.sharedFolderId ?: return emptyList())
            } else {
                getFilePermissions(fileId)
            }
        } catch (exception: DbxException) {
            throw ExceptionMapper.toOmhApiException(exception)
        }
    }

    fun getStorageUsage(): Long {
        try {
            return apiService.getSpaceUsage().used
        } catch (exception: DbxException) {
            throw ExceptionMapper.toOmhApiException(exception)
        }
    }

    fun getStorageQuota(): Long {
        try {
            val spaceAllocation = apiService.getSpaceUsage().allocation
            return if (spaceAllocation.isIndividual) {
                spaceAllocation.individualValue.allocated
            } else if (spaceAllocation.isTeam) {
                spaceAllocation.teamValue.allocated
            } else {
                -1L // For OTHER
            }
        } catch (exception: DbxException) {
            throw ExceptionMapper.toOmhApiException(exception)
        }
    }

    private fun getFilePermissions(fileId: String): List<OmhPermission> {
        val result = apiService.getFilePermissions(fileId)

        return result.invitees.mapNotNull { it.toOmhPermission() }
            .plus(result.users.mapNotNull { it.toOmhPermission() })
            .plus(result.groups.mapNotNull { it.toOmhPermission() })
    }

    private fun getFolderPermissions(sharedFolderId: String): List<OmhPermission> {
        val result = apiService.getFolderPermissions(sharedFolderId)

        return result.invitees.mapNotNull { it.toOmhPermission() }
            .plus(result.users.mapNotNull { it.toOmhPermission() })
            .plus(result.groups.mapNotNull { it.toOmhPermission() })
    }

    private fun isFolder(fileId: String): FolderMetadata? {
        return apiService.getFile(fileId) as? FolderMetadata
    }

    fun resolvePath(path: String): OmhStorageEntity? {
        val nodeId = apiService.queryNodeIdHaving(path)
        return nodeId?.let {
            metadataToOmhStorageEntity(apiService.getFile(it))
        }
    }
}
