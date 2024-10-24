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
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.body.CreatePermissionRequestBody
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.body.UpdatePermissionRequestBody
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.AboutResponse
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.FileListRemoteResponse
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.FileRemoteResponse
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.PermissionResponse
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.PermissionsListResponse
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.RevisionListRemoteResponse
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.WebUrlResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

@Suppress("TooManyFunctions")
internal interface GoogleStorageApiService {

    companion object {
        private const val ABOUT = "drive/v3/about"
        private const val FILES_PARTICLE = "drive/v3/files"
        private const val UPLOAD_FILES_PARTICLE = "upload/drive/v3/files"

        private const val QUERY_Q = "q"
        private const val QUERY_FIELDS = "fields"
        private const val QUERY_MIME_TYPE = "mimeType"
        private const val QUERY_ALT = "alt"
        private const val QUERY_TRANSFER_OWNERSHIP = "transferOwnership"
        private const val QUERY_SEND_NOTIFICATION_EMAIL = "sendNotificationEmail"
        private const val QUERY_EMAIL_MESSAGE = "emailMessage"
        private const val QUERY_UPLOAD_TYPE = "uploadType"

        private const val PARENT_ID_Q_VALUE = "'%s' in parents and trashed = false"
        private const val SEARCH_BY_NAME_Q_VALUE = "name contains '%s' and trashed = false"

        internal fun getParentIdQuery(parentId: String = "root") =
            String.format(PARENT_ID_Q_VALUE, parentId)

        internal fun getSearchByNameQuery(query: String) =
            String.format(SEARCH_BY_NAME_Q_VALUE, query)

        internal const val QUERY_REQUESTED_FIELDS =
            "id,name,createdTime,modifiedTime,parents,mimeType,fileExtension,size"
        private const val FIELDS_VALUE = "files($QUERY_REQUESTED_FIELDS)"
        private const val QUERY_REQUESTED_FIELDS_ALL = "*"
        private const val QUERY_PERMISSIONS = "permissions"
        private const val QUERY_WEB_URL = "webViewLink"

        private const val FILE_ID = "fileId"
        private const val REVISION_ID = "revisionId"
        private const val PERMISSION_ID = "permissionId"
    }

    @GET("$FILES_PARTICLE/{$FILE_ID}")
    suspend fun getFile(
        @Path(FILE_ID) fileId: String
    ): Response<FileRemoteResponse>

    @GET(FILES_PARTICLE)
    suspend fun getFilesList(
        @Query(QUERY_Q) query: String,
        @Query(QUERY_FIELDS) fields: String = FIELDS_VALUE
    ): Response<FileListRemoteResponse>

    @POST(FILES_PARTICLE)
    suspend fun createFile(
        @Body body: CreateFileRequestBody,
        @Query(QUERY_FIELDS) fields: String = QUERY_REQUESTED_FIELDS,
    ): Response<FileRemoteResponse>

    @DELETE("$FILES_PARTICLE/{$FILE_ID}")
    suspend fun deleteFile(
        @Path(FILE_ID) fileId: String
    ): Response<ResponseBody>

    @GET("$FILES_PARTICLE/{$FILE_ID}")
    suspend fun downloadMediaFile(
        @Path(FILE_ID) fileId: String,
        @Query(QUERY_ALT) alt: String
    ): Response<ResponseBody>

    @GET("$FILES_PARTICLE/{$FILE_ID}")
    suspend fun getPermissions(
        @Path(FILE_ID) fileId: String,
        @Query(QUERY_FIELDS) fields: String = QUERY_PERMISSIONS
    ): Response<PermissionsListResponse>

    @GET("$FILES_PARTICLE/{$FILE_ID}/export")
    suspend fun exportFile(
        @Path(FILE_ID) fileId: String,
        @Query(QUERY_MIME_TYPE) mimeType: String
    ): Response<ResponseBody>

    @Multipart
    @POST(UPLOAD_FILES_PARTICLE)
    suspend fun uploadFile(
        @Part metadata: MultipartBody.Part,
        @Part file: MultipartBody.Part,
        @Query("uploadType") uploadType: String = "multipart",
        @Query(QUERY_FIELDS) fields: String = QUERY_REQUESTED_FIELDS,
    ): Response<FileRemoteResponse>

    @Multipart
    @PATCH("$UPLOAD_FILES_PARTICLE/{$FILE_ID}")
    suspend fun updateFile(
        @Path(FILE_ID) fileId: String,
        @Part metadata: MultipartBody.Part,
        @Part file: MultipartBody.Part,
        @Query("uploadType") uploadType: String = "multipart",
        @Query(QUERY_FIELDS) fields: String = QUERY_REQUESTED_FIELDS,
    ): Response<FileRemoteResponse>

    @PATCH("$FILES_PARTICLE/{$FILE_ID}")
    suspend fun updateMetaData(
        @Body filePart: RequestBody,
        @Path(FILE_ID) fileId: String
    ): Response<FileRemoteResponse>

    @GET("$FILES_PARTICLE/{$FILE_ID}/revisions")
    suspend fun getFileRevisions(
        @Path(FILE_ID) fileId: String
    ): Response<RevisionListRemoteResponse>

    @GET("$FILES_PARTICLE/{$FILE_ID}/revisions/{$REVISION_ID}")
    suspend fun downloadFileRevision(
        @Path(FILE_ID) fileId: String,
        @Path(REVISION_ID) revisionId: String,
        @Query(QUERY_ALT) alt: String
    ): Response<ResponseBody>

    @GET("$FILES_PARTICLE/{$FILE_ID}")
    suspend fun getFileMetadata(
        @Path(FILE_ID) fileId: String,
        @Query(QUERY_FIELDS) fields: String = "*"
    ): Response<ResponseBody>

    @DELETE("$FILES_PARTICLE/{$FILE_ID}/permissions/{$PERMISSION_ID}")
    suspend fun deletePermission(
        @Path(FILE_ID) fileId: String,
        @Path(PERMISSION_ID) permissionId: String
    ): Response<ResponseBody>

    @Suppress("LongParameterList")
    @PATCH("$FILES_PARTICLE/{$FILE_ID}/permissions/{$PERMISSION_ID}")
    suspend fun updatePermission(
        @Path(FILE_ID) fileId: String,
        @Path(PERMISSION_ID) permissionId: String,
        @Body body: UpdatePermissionRequestBody,
        @Query(QUERY_TRANSFER_OWNERSHIP) transferOwnership: Boolean = false,
        @Query(QUERY_SEND_NOTIFICATION_EMAIL) sendNotificationEmail: Boolean = false,
        @Query(QUERY_FIELDS) fields: String = QUERY_REQUESTED_FIELDS_ALL,
    ): Response<PermissionResponse>

    @Suppress("LongParameterList")
    @POST("$FILES_PARTICLE/{$FILE_ID}/permissions")
    suspend fun createPermission(
        @Path(FILE_ID) fileId: String,
        @Body body: CreatePermissionRequestBody,
        @Query(QUERY_TRANSFER_OWNERSHIP) transferOwnership: Boolean? = false,
        @Query(QUERY_SEND_NOTIFICATION_EMAIL) sendNotificationEmail: Boolean? = false,
        @Query(QUERY_EMAIL_MESSAGE) emailMessage: String? = null,
        @Query(QUERY_FIELDS) fields: String = QUERY_REQUESTED_FIELDS_ALL,
    ): Response<PermissionResponse>

    @GET("$FILES_PARTICLE/{$FILE_ID}")
    suspend fun getWebUrl(
        @Path(FILE_ID) fileId: String,
        @Query(QUERY_FIELDS) fields: String = QUERY_WEB_URL,
    ): Response<WebUrlResponse>

    @POST(UPLOAD_FILES_PARTICLE)
    suspend fun postResumableUpload(
        @Body body: RequestBody,
        @Query(QUERY_UPLOAD_TYPE) uploadType: String = "resumable",
        @Query(QUERY_FIELDS) fields: String = QUERY_REQUESTED_FIELDS,
    ): Response<ResponseBody>

    @PUT("$UPLOAD_FILES_PARTICLE/{$FILE_ID}")
    suspend fun putResumableUpload(
        @Path(FILE_ID) fileId: String,
        @Body body: RequestBody,
        @Query(QUERY_UPLOAD_TYPE) uploadType: String = "resumable",
        @Query(QUERY_FIELDS) fields: String = QUERY_REQUESTED_FIELDS,
    ): Response<ResponseBody>

    @PUT
    suspend fun uploadFileChunk(
        @Url url: String,
        @Header("Content-Length") contentLength: Long,
        @Header("Content-Range") contentRange: String,
        @Body fileChunk: RequestBody,
    ): Response<FileRemoteResponse>

    @GET(ABOUT)
    suspend fun about(): Response<AboutResponse>
}
