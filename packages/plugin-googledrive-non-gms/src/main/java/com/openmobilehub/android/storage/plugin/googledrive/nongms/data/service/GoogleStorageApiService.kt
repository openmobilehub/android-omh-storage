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

package com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service

import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.body.CreateFileRequestBody
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.FileListRemoteResponse
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.FileRemoteResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

internal interface GoogleStorageApiService {

    companion object {
        private const val FILES_PARTICLE = "drive/v3/files"
        private const val UPLOAD_FILES_PARTICLE = "upload/drive/v3/files"

        private const val QUERY_Q = "q"
        private const val QUERY_FIELDS = "fields"
        private const val QUERY_MIME_TYPE = "mimeType"
        private const val QUERY_ALT = "alt"

        private const val Q_VALUE = "'%s' in parents and trashed = false"

        internal fun getQueryValue(parentId: String = "root") = String.format(Q_VALUE, parentId)

        private const val QUERY_REQUESTED_FIELDS = "id,name,mimeType,modifiedTime,parents"
        private const val FIELDS_VALUE = "files($QUERY_REQUESTED_FIELDS)"

        private const val FILE_ID = "fileId"
        private const val META_DATA = "metadata"
    }

    @GET(FILES_PARTICLE)
    suspend fun getFilesList(
        @Query(QUERY_Q) query: String,
        @Query(QUERY_FIELDS) fields: String = FIELDS_VALUE
    ): Response<FileListRemoteResponse>

    @POST(FILES_PARTICLE)
    suspend fun createFile(
        @Query(QUERY_FIELDS) query: String = QUERY_REQUESTED_FIELDS,
        @Body body: CreateFileRequestBody
    ): Response<FileRemoteResponse>

    @DELETE("$FILES_PARTICLE/{$FILE_ID}")
    suspend fun deleteFile(
        @Path(FILE_ID) fileId: String
    ): Response<ResponseBody>

    @Multipart
    @POST(UPLOAD_FILES_PARTICLE)
    suspend fun uploadFile(
        @Part(META_DATA) metadata: RequestBody,
        @Part filePart: MultipartBody.Part
    ): Response<FileRemoteResponse>

    @GET("$FILES_PARTICLE/{$FILE_ID}")
    suspend fun downloadMediaFile(
        @Path(FILE_ID) fileId: String,
        @Query(QUERY_ALT) alt: String
    ): Response<ResponseBody>

    @GET("$FILES_PARTICLE/{$FILE_ID}/export")
    suspend fun exportDocEditor(
        @Path(FILE_ID) fileId: String,
        @Query(QUERY_MIME_TYPE) mimeType: String
    ): Response<ResponseBody>

    @PATCH("$UPLOAD_FILES_PARTICLE/{$FILE_ID}")
    suspend fun updateFile(
        @Body filePart: RequestBody,
        @Path(FILE_ID) fileId: String
    ): Response<FileRemoteResponse>

    @PATCH("$FILES_PARTICLE/{$FILE_ID}")
    suspend fun updateMetaData(
        @Body filePart: RequestBody,
        @Path(FILE_ID) fileId: String
    ): Response<FileRemoteResponse>
}
