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

import android.webkit.MimeTypeMap
import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.model.OmhStorageMetadata
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
    private val retrofitImpl: GoogleStorageApiServiceProvider
) {

    companion object {
        private const val FILE_NAME_KEY = "name"
        private const val FILE_PARENTS_KEY = "parents"
        private const val FILE_TRASHED_KEY = "trashed"
        private const val ANY_MIME_TYPE = "*/*"
        private const val MEDIA = "media"

        private val JSON_MIME_TYPE = "application/json".toMediaTypeOrNull()
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
            null
        }
    }

    suspend fun permanentlyDeleteFile(fileId: String): Boolean {
        val response = retrofitImpl
            .getGoogleStorageApiService()
            .deleteFile(
                fileId = fileId
            )

        return response.isSuccessful
    }

    suspend fun deleteFile(fileId: String): Boolean {
        val jsonMetaData = JSONObject().apply {
            put(FILE_TRASHED_KEY, true)
        }
        val jsonRequestBody = jsonMetaData.toString().toRequestBody(null)

        val response = retrofitImpl
            .getGoogleStorageApiService().updateMetaData(jsonRequestBody, fileId)

        return response.isSuccessful
    }

    suspend fun uploadFile(
        localFileToUpload: File,
        parentId: String?
    ): OmhStorageEntity? {
        val stringMimeType = MimeTypeMap
            .getSingleton()
            .getMimeTypeFromExtension(localFileToUpload.extension)
            ?: ANY_MIME_TYPE

        val mimeType = stringMimeType.toMediaTypeOrNull()
        val requestFile = localFileToUpload.asRequestBody(mimeType)
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
        val filePart =
            MultipartBody.Part.createFormData(FILE_NAME_KEY, localFileToUpload.name, requestFile)

        val response = retrofitImpl
            .getGoogleStorageApiService()
            .uploadFile(jsonRequestBody, filePart)

        return if (response.isSuccessful) {
            response.body()?.toOmhStorageEntity()
        } else {
            null
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

    suspend fun updateFile(
        localFileToUpload: File,
        fileId: String
    ): OmhStorageEntity.OmhFile? {
        val jsonMetaData = JSONObject().apply {
            put(FILE_NAME_KEY, localFileToUpload.name)
        }

        val jsonRequestBody = jsonMetaData.toString().toRequestBody(JSON_MIME_TYPE)
        val response = retrofitImpl
            .getGoogleStorageApiService()
            .updateMetaData(jsonRequestBody, fileId)

        return if (response.isSuccessful) {
            val omhStorageEntity = response.body()?.toOmhStorageEntity() ?: return null
            updateMediaFile(localFileToUpload, omhStorageEntity)
        } else {
            throw response.toApiException()
        }
    }

    private suspend fun updateMediaFile(
        localFileToUpload: File,
        omhStorageEntity: OmhStorageEntity
    ): OmhStorageEntity.OmhFile? {
        val mimeType =
            if (omhStorageEntity is OmhStorageEntity.OmhFile) {
                omhStorageEntity.mimeType?.toMediaTypeOrNull()
            } else {
                null
            }
        val requestFile = localFileToUpload.asRequestBody(mimeType)

        val response = retrofitImpl
            .getGoogleStorageApiService()
            .updateFile(requestFile, omhStorageEntity.id)

        return if (response.isSuccessful) {
            response.body()?.toOmhStorageEntity() as? OmhStorageEntity.OmhFile
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

    suspend fun deletePermission(fileId: String, permissionId: String): Boolean {
        val response = retrofitImpl
            .getGoogleStorageApiService()
            .deletePermission(
                fileId = fileId,
                permissionId = permissionId
            )

        return response.isSuccessful
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

    @Suppress("SwallowedException")
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
}
