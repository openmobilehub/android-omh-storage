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

package com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository

import androidx.annotation.VisibleForTesting
import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.model.OmhStorageMetadata
import com.openmobilehub.android.storage.core.utils.splitPathToParts
import com.openmobilehub.android.storage.core.utils.toInputStream
import com.openmobilehub.android.storage.plugin.googledrive.nongms.GoogleDriveNonGmsConstants
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.mapper.LocalFileToMimeType
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.mapper.toCreateRequestBody
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.mapper.toFileList
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.mapper.toOmhFileVersions
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.mapper.toOmhStorageEntity
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.mapper.toPermission
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.mapper.toPermissions
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.mapper.toUpdateRequestBody
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.GoogleStorageApiService
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.body.CreateFileRequestBody
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.retrofit.GoogleStorageApiServiceProvider
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.utils.isNotSuccessful
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.utils.toApiException
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.utils.toByteArrayOutputStream
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.utils.toOmhStorageEntityMetadata
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File

@Suppress("TooManyFunctions")
internal class NonGmsFileRepository(
    private val retrofitImpl: GoogleStorageApiServiceProvider,
    private val localFileToMimeType: LocalFileToMimeType
) {

    companion object {
        private const val FILE_NAME_KEY = "name"
        private const val MIME_TYPE_KEY = "mimeType"
        private const val FILE_PARENTS_KEY = "parents"
        private const val FILE_TRASHED_KEY = "trashed"
        private const val MEDIA = "media"

        private const val LOCATION_HEADER = "Location"

        private val JSON_MIME_TYPE = "application/json".toMediaTypeOrNull()

        private const val UPLOAD_CHUNK_SIZE = 1024 * 1024 * 10 // 10MB
        private const val SMALL_FILE_SIZE = 1024 * 1024 // 1MB
        private const val DEFAULT_UPLOAD_MIME_TYPE = "application/octet-stream"
        private const val RESUME_INCOMPLETE_STATUS_CODE = 308
    }

    suspend fun getFilesList(parentId: String): List<OmhStorageEntity> {
        return getFiles(GoogleStorageApiService.getParentIdQuery(parentId))
    }

    suspend fun search(query: String): List<OmhStorageEntity> {
        return getFiles(GoogleStorageApiService.getSearchByNameQuery(query))
    }

    private suspend fun getFiles(query: String): List<OmhStorageEntity> {
        val response = retrofitImpl
            .getGoogleStorageApiService()
            .getFilesList(
                query = query,
            )

        return if (response.isSuccessful) {
            response.body()?.toFileList().orEmpty()
        } else {
            throw response.toApiException()
        }
    }

    suspend fun createFile(name: String, mimeType: String, parentId: String?): OmhStorageEntity? {
        val parents = if (parentId.isNullOrBlank()) {
            emptyList()
        } else {
            listOf(parentId)
        }

        val response = retrofitImpl
            .getGoogleStorageApiService()
            .createFile(body = CreateFileRequestBody(mimeType, name, parents))

        return if (response.isSuccessful) {
            response.body()?.toOmhStorageEntity()
        } else {
            throw response.toApiException()
        }
    }

    suspend fun createFolder(name: String, parentId: String): OmhStorageEntity? {
        return createFile(name, GoogleDriveNonGmsConstants.FOLDER_MIME_TYPE, parentId)
    }

    suspend fun permanentlyDeleteFile(fileId: String) {
        val response = retrofitImpl
            .getGoogleStorageApiService()
            .deleteFile(
                fileId = fileId
            )

        if (response.isNotSuccessful) {
            throw response.toApiException()
        }
    }

    suspend fun deleteFile(fileId: String) {
        val jsonMetaData = JSONObject().apply {
            put(FILE_TRASHED_KEY, true)
        }
        val jsonRequestBody = jsonMetaData.toString().toRequestBody(null)

        val response = retrofitImpl
            .getGoogleStorageApiService().updateMetaData(jsonRequestBody, fileId)

        if (response.isNotSuccessful) {
            throw response.toApiException()
        }
    }

    suspend fun uploadFile(
        localFileToUpload: File,
        parentId: String?
    ): OmhStorageEntity? {
        if (localFileToUpload.length() < SMALL_FILE_SIZE) {
            return uploadSmallFile(localFileToUpload, parentId)
        } else {
            val uploadUrl = initializeResumableUpload(localFileToUpload, parentId)
            return uploadFileChunks(uploadUrl, localFileToUpload)
        }
    }

    suspend fun updateFile(
        localFileToUpload: File,
        fileId: String
    ): OmhStorageEntity.OmhFile {
        if (localFileToUpload.length() < SMALL_FILE_SIZE) {
            return smallFileUpdate(localFileToUpload, fileId) as OmhStorageEntity.OmhFile
        } else {
            val uploadUrl = initializeResumableUpdate(localFileToUpload, fileId)
            return uploadFileChunks(uploadUrl, localFileToUpload) as OmhStorageEntity.OmhFile
        }
    }

    @VisibleForTesting
    suspend fun initializeResumableUpload(
        localFileToUpload: File,
        parentId: String?
    ): String {
        val parentsList = if (parentId.isNullOrBlank()) {
            emptyList()
        } else {
            listOf(parentId)
        }

        val parentsListAsJson = JSONArray(parentsList)
        val jsonMetaData = JSONObject().apply {
            put(FILE_NAME_KEY, localFileToUpload.name)
            put(FILE_PARENTS_KEY, parentsListAsJson)
        }

        val jsonRequestBody = jsonMetaData.toString().toRequestBody(JSON_MIME_TYPE)

        val response = retrofitImpl
            .getGoogleStorageApiService()
            .postResumableUpload(jsonRequestBody)

        if (!response.isSuccessful) {
            throw response.toApiException()
        }

        return response.headers()[LOCATION_HEADER]
            ?: throw OmhStorageException.ApiException(
                message = "Location header is missing from the response"
            )
    }

    @VisibleForTesting
    suspend fun initializeResumableUpdate(
        localFileToUpload: File,
        fileId: String
    ): String {
        val jsonMetaData = JSONObject().apply {
            put(FILE_NAME_KEY, localFileToUpload.name)
            put(MIME_TYPE_KEY, localFileToMimeType(localFileToUpload) ?: DEFAULT_UPLOAD_MIME_TYPE)
        }

        val jsonRequestBody = jsonMetaData.toString().toRequestBody(JSON_MIME_TYPE)

        val response = retrofitImpl
            .getGoogleStorageApiService()
            .putResumableUpload(fileId = fileId, body = jsonRequestBody)

        if (!response.isSuccessful) {
            throw response.toApiException()
        }

        return response.headers()[LOCATION_HEADER]
            ?: throw OmhStorageException.ApiException(
                message = "Location header is missing from the response"
            )
    }

    @VisibleForTesting
    suspend fun uploadFileChunks(
        uploadUrl: String,
        file: File,
    ): OmhStorageEntity? {
        val fileLength = file.length()
        val inputStream = file.toInputStream()
        var uploadedBytes: Long = 0

        inputStream.use { stream ->
            while (uploadedBytes < fileLength) {
                val remainingBytes = fileLength - uploadedBytes
                val bytesToRead =
                    if (remainingBytes < UPLOAD_CHUNK_SIZE) remainingBytes else UPLOAD_CHUNK_SIZE

                val buffer = ByteArray(bytesToRead.toInt())
                val bytesRead = stream.read(buffer, 0, bytesToRead.toInt())
                if (bytesRead == -1) break

                val mimeType = localFileToMimeType(file) ?: DEFAULT_UPLOAD_MIME_TYPE

                val fileChunk = buffer.toRequestBody(
                    mimeType.toMediaTypeOrNull(),
                    0,
                )

                val contentRange =
                    "bytes $uploadedBytes-${uploadedBytes + bytesRead - 1}/$fileLength"
                val response = retrofitImpl.getGoogleStorageApiService().uploadFileChunk(
                    uploadUrl,
                    bytesRead.toLong(),
                    contentRange,
                    fileChunk
                )

                when {
                    response.isSuccessful -> return response.body()?.toOmhStorageEntity()
                        ?: throw OmhStorageException.ApiException(
                            message = "Failed to map response to OmhStorageEntity"
                        )

                    response.code() == RESUME_INCOMPLETE_STATUS_CODE -> {
                        uploadedBytes += bytesRead
                    }

                    else -> throw OmhStorageException.ApiException(
                        message = "Failed to upload file chunk",
                        cause = response.toApiException()
                    )
                }
            }
        }
        return null
    }

    @VisibleForTesting
    suspend fun uploadSmallFile(
        localFileToUpload: File,
        parentId: String?
    ): OmhStorageEntity? {
        val mimeType = localFileToMimeType(localFileToUpload) ?: DEFAULT_UPLOAD_MIME_TYPE

        val parentsList = if (parentId.isNullOrBlank()) {
            emptyList()
        } else {
            listOf(parentId)
        }
        val parentsListAsJson = JSONArray(parentsList)

        val jsonMetaData = JSONObject().apply {
            put(FILE_NAME_KEY, localFileToUpload.name)
            put(FILE_PARENTS_KEY, parentsListAsJson)
        }

        val jsonRequestBody = jsonMetaData.toString().toRequestBody(JSON_MIME_TYPE)
        val metadataPart = MultipartBody.Part.createFormData("metadata", null, jsonRequestBody)

        val fileRequestBody = localFileToUpload.asRequestBody(mimeType.toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("media", localFileToUpload.name, fileRequestBody)

        val response = retrofitImpl
            .getGoogleStorageApiService()
            .uploadFile(
                metadata = metadataPart,
                file = filePart
            )

        return if (response.isSuccessful) {
            response.body()?.toOmhStorageEntity()
        } else {
            throw response.toApiException()
        }
    }

    @VisibleForTesting
    suspend fun smallFileUpdate(
        localFileToUpload: File,
        fileId: String
    ): OmhStorageEntity? {
        val mimeType = localFileToMimeType(localFileToUpload) ?: DEFAULT_UPLOAD_MIME_TYPE

        val jsonMetaData = JSONObject().apply {
            put(FILE_NAME_KEY, localFileToUpload.name)
            put(MIME_TYPE_KEY, mimeType)
        }

        val jsonRequestBody = jsonMetaData.toString().toRequestBody(JSON_MIME_TYPE)
        val metadataPart = MultipartBody.Part.createFormData("metadata", null, jsonRequestBody)

        val fileRequestBody = localFileToUpload.asRequestBody(mimeType.toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("media", localFileToUpload.name, fileRequestBody)

        val response = retrofitImpl
            .getGoogleStorageApiService()
            .updateFile(
                fileId = fileId,
                metadata = metadataPart,
                file = filePart
            )

        return if (response.isSuccessful) {
            response.body()?.toOmhStorageEntity()
        } else {
            throw response.toApiException()
        }
    }

    suspend fun downloadFile(fileId: String): ByteArrayOutputStream {
        val response = retrofitImpl
            .getGoogleStorageApiService()
            .downloadMediaFile(fileId = fileId, alt = MEDIA)

        return if (response.isSuccessful) {
            response.body().toByteArrayOutputStream()
        } else {
            throw response.toApiException()
        }
    }

    suspend fun exportFile(fileId: String, mimeType: String): ByteArrayOutputStream {
        val response = retrofitImpl
            .getGoogleStorageApiService()
            .exportFile(fileId, mimeType)

        return if (response.isSuccessful) {
            response.body().toByteArrayOutputStream()
        } else {
            throw response.toApiException()
        }
    }

    suspend fun getFileVersions(fileId: String): List<OmhFileVersion> {
        val response = retrofitImpl
            .getGoogleStorageApiService()
            .getFileRevisions(
                fileId = fileId
            )

        return if (response.isSuccessful) {
            response.body()?.toOmhFileVersions(fileId).orEmpty().reversed()
        } else {
            throw response.toApiException()
        }
    }

    suspend fun downloadFileVersion(fileId: String, versionId: String): ByteArrayOutputStream {
        val response = retrofitImpl
            .getGoogleStorageApiService()
            .downloadFileRevision(fileId = fileId, revisionId = versionId, alt = MEDIA)

        return if (response.isSuccessful) {
            response.body().toByteArrayOutputStream()
        } else {
            throw response.toApiException()
        }
    }

    suspend fun getFilePermissions(fileId: String): List<OmhPermission> {
        val response = retrofitImpl
            .getGoogleStorageApiService()
            .getPermissions(
                fileId = fileId
            )

        return if (response.isSuccessful) {
            response.body()?.toPermissions().orEmpty()
        } else {
            throw response.toApiException()
        }
    }

    suspend fun deletePermission(fileId: String, permissionId: String) {
        val response = retrofitImpl
            .getGoogleStorageApiService()
            .deletePermission(
                fileId = fileId,
                permissionId = permissionId
            )

        if (response.isNotSuccessful) {
            throw response.toApiException()
        }
    }

    suspend fun updatePermission(
        fileId: String,
        permissionId: String,
        role: OmhPermissionRole
    ): OmhPermission {
        val transferOwnership = role == OmhPermissionRole.OWNER
        val response = retrofitImpl
            .getGoogleStorageApiService()
            .updatePermission(
                fileId = fileId,
                permissionId = permissionId,
                body = role.toUpdateRequestBody(),
                transferOwnership = transferOwnership,
                // need to be set to true when transfer ownership
                sendNotificationEmail = transferOwnership,
            )
        if (response.isSuccessful) {
            return response.body()?.toPermission() ?: throw OmhStorageException.ApiException(
                message = "Updated succeeded but API failed to return expected permission"
            )
        } else {
            throw response.toApiException()
        }
    }

    suspend fun createPermission(
        fileId: String,
        permission: OmhCreatePermission,
        sendNotificationEmail: Boolean,
        emailMessage: String?
    ): OmhPermission {
        val transferOwnership = permission.role == OmhPermissionRole.OWNER
        val message = emailMessage?.ifBlank { null }
        val willSendNotificationEmail = sendNotificationEmail || transferOwnership
        val response = retrofitImpl
            .getGoogleStorageApiService()
            .createPermission(
                fileId = fileId,
                body = permission.toCreateRequestBody(),
                transferOwnership = transferOwnership,
                // need to be set to true when transfer ownership
                sendNotificationEmail = willSendNotificationEmail,
                emailMessage = if (willSendNotificationEmail) message else null,
            )
        if (response.isSuccessful) {
            return response.body()?.toPermission() ?: throw OmhStorageException.ApiException(
                message = "Create succeeded but API failed to return expected permission"
            )
        } else {
            throw response.toApiException()
        }
    }

    suspend fun getFileMetadata(fileId: String): OmhStorageMetadata {
        val response = retrofitImpl
            .getGoogleStorageApiService()
            .getFileMetadata(fileId = fileId)

        return if (response.isSuccessful) {
            response.body().toOmhStorageEntityMetadata()
        } else {
            throw response.toApiException()
        }
    }

    suspend fun getWebUrl(fileId: String): String? {
        val response = retrofitImpl
            .getGoogleStorageApiService()
            .getWebUrl(fileId = fileId)

        return if (response.isSuccessful) {
            response.body()?.webViewLink
        } else {
            throw response.toApiException()
        }
    }

    suspend fun getStorageUsage(): Long {
        val response = retrofitImpl.getGoogleStorageApiService().about()

        return if (response.isSuccessful) {
            response.body()!!.storageQuota.usageInDrive
        } else {
            throw response.toApiException()
        }
    }

    suspend fun getStorageQuota(): Long {
        val response = retrofitImpl.getGoogleStorageApiService().about()

        return if (response.isSuccessful) {
            response.body()!!.storageQuota.limit
        } else {
            throw response.toApiException()
        }
    }

    suspend fun resolvePath(path: String): OmhStorageEntity? {
        val parts = path.splitPathToParts()
        var parentId = GoogleDriveNonGmsConstants.ROOT_FOLDER
        for (nodeName in parts) {
            val entries = getFiles("'$parentId' in parents and name='$nodeName'")
            if (entries.isEmpty()) {
                return null
            } else {
                parentId = entries.first().id
            }
        }

        return retrofitImpl.getGoogleStorageApiService().getFile(parentId).body()?.toOmhStorageEntity()
    }
}
