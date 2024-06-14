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
import com.openmobilehub.android.storage.core.model.OmhFile
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.model.OmhStorageStatusCodes
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.mapper.toFile
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.mapper.toFileList
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.GoogleStorageApiService
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.body.CreateFileRequestBody
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.retrofit.GoogleStorageApiServiceProvider
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.utils.toByteArrayOutputStream
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException
import java.io.ByteArrayOutputStream
import java.io.File

internal class NonGmsFileRepository(
    private val retrofitImpl: GoogleStorageApiServiceProvider
) {

    companion object {
        private const val FILE_NAME_KEY = "name"
        private const val FILE_PARENTS_KEY = "parents"
        private const val ANY_MIME_TYPE = "*/*"
        private const val MEDIA = "media"

        private val JSON_MIME_TYPE = "application/json".toMediaTypeOrNull()
    }

    suspend fun getFilesList(parentId: String): List<OmhFile> {
        return getFiles(GoogleStorageApiService.getParentIdQuery(parentId))
    }

    suspend fun search(query: String): List<OmhFile> {
        return getFiles(GoogleStorageApiService.getSearchByNameQuery(query))
    }

    private suspend fun getFiles(query: String): List<OmhFile> {
        val response = retrofitImpl
            .getGoogleStorageApiService()
            .getFilesList(
                query = query
            )

        return if (response.isSuccessful) {
            response.body()?.toFileList().orEmpty()
        } else {
            throw OmhStorageException.ApiException(response.code(), HttpException(response))
        }
    }

    suspend fun createFile(name: String, mimeType: String, parentId: String?): OmhFile? {
        val parents = if (parentId.isNullOrBlank()) {
            emptyList()
        } else {
            listOf(parentId)
        }

        val response = retrofitImpl
            .getGoogleStorageApiService()
            .createFile(body = CreateFileRequestBody(mimeType, name, parents))

        return if (response.isSuccessful) {
            response.body()?.toFile()
        } else {
            null
        }
    }

    suspend fun deleteFile(fileId: String): Boolean {
        val response = retrofitImpl
            .getGoogleStorageApiService()
            .deleteFile(
                fileId = fileId
            )

        return response.isSuccessful
    }

    suspend fun uploadFile(
        localFileToUpload: File,
        parentId: String?
    ): OmhFile? {
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
            response.body()?.toFile()
        } else {
            null
        }
    }

    suspend fun downloadFile(fileId: String, mimeType: String?): ByteArrayOutputStream {
        val response = retrofitImpl
            .getGoogleStorageApiService()
            .downloadMediaFile(fileId = fileId, alt = MEDIA)

        return if (response.isSuccessful) {
            response.body().toByteArrayOutputStream()
        } else {
            if (mimeType == null) {
                val cause = HttpException(response)
                throw OmhStorageException.DownloadException(
                    OmhStorageStatusCodes.DOWNLOAD_ERROR,
                    cause
                )
            }

            return exportDocEditor(fileId, mimeType)
        }
    }

    private suspend fun exportDocEditor(fileId: String, mimeType: String): ByteArrayOutputStream {
        val response = retrofitImpl
            .getGoogleStorageApiService()
            .exportDocEditor(fileId, mimeType)

        return if (response.isSuccessful) {
            response.body().toByteArrayOutputStream()
        } else {
            val cause = HttpException(response)
            throw OmhStorageException.DownloadException(
                OmhStorageStatusCodes.DOWNLOAD_GOOGLE_WORKSPACE_ERROR,
                cause
            )
        }
    }

    suspend fun updateFile(
        localFileToUpload: File,
        fileId: String
    ): OmhFile? {
        val jsonMetaData = JSONObject().apply {
            put(FILE_NAME_KEY, localFileToUpload.name)
        }

        val jsonRequestBody = jsonMetaData.toString().toRequestBody(JSON_MIME_TYPE)
        val response = retrofitImpl
            .getGoogleStorageApiService()
            .updateMetaData(jsonRequestBody, fileId)

        return if (response.isSuccessful) {
            val omhFile = response.body()?.toFile() ?: return null
            updateMediaFile(localFileToUpload, omhFile)
        } else {
            throw OmhStorageException.UpdateException(
                OmhStorageStatusCodes.UPDATE_META_DATA,
                HttpException(response)
            )
        }
    }

    private suspend fun updateMediaFile(
        localFileToUpload: File,
        omhFile: OmhFile
    ): OmhFile? {
        val mimeType = omhFile.mimeType.toMediaTypeOrNull()
        val requestFile = localFileToUpload.asRequestBody(mimeType)

        val response = retrofitImpl
            .getGoogleStorageApiService()
            .updateFile(requestFile, omhFile.id)

        return if (response.isSuccessful) {
            response.body()?.toFile()
        } else {
            throw OmhStorageException.UpdateException(
                OmhStorageStatusCodes.UPDATE_CONTENT_FILE,
                HttpException(response)
            )
        }
    }
}
