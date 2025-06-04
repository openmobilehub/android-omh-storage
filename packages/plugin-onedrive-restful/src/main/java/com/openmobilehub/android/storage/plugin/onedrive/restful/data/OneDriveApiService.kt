package com.openmobilehub.android.storage.plugin.onedrive.restful.data

import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.body.CreateFolderRequestBody
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.body.CreatePermissionRequestBody
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.body.UpdatePermissionRequestBody
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response.Drive
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response.DriveItem
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response.FileVersionsListResponse
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response.ListFolderResponse
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response.PermissionResponse
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response.PermissionsListResponse
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response.UploadSessionResponse
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

@Suppress("TooManyFunctions")
interface OneDriveApiService {
    companion object {
        const val DRIVE = "drive"
        const val ROOT = "root"
        const val APPLICATION_OCTET_STREAM = "application/octet-stream"

        private const val CONTENT_TYPE = "Content-Type"

        private const val V1 = "v1.0"

        private const val ME = "me"
        private const val ITEMS = "items"
        private const val FOLDER_ID = "folderId"
        private const val ITEM_ID = "itemId"
        private const val CHILDREN = "children"
        private const val CONTENT = "content"
        private const val QUERY_SKIPTOKEN = "\$skiptoken"
        private const val PATH = "path"
        private const val CREATE_UPLOAD_SESSION = "createUploadSession"
        private const val Q = "q"

        // Permissions
        private const val PERMISSIONS = "permissions"
        private const val INVITE = "invite"
        private const val PERMISSION_ID = "permissionId"
        private const val VERSIONS = "versions"
        private const val VERSION_ID = "versionId"
    }

    @GET("$V1/$ME/$DRIVE")
    suspend fun getDrive(): Response<Drive>

    // validate PATH required.
    @GET("$V1/$DRIVE/$ROOT:{$PATH}")
    suspend fun getItemByPath(
        @Path(value = PATH, encoded = true) path: String,
    ): Response<DriveItem>

    @GET("$V1/$DRIVE/$ITEMS/{$ITEM_ID}")
    suspend fun getItemById(
        @Path(ITEM_ID, encoded = true) id: String,
    ): Response<DriveItem>

    @GET("$V1/$DRIVE/$ROOT/$CHILDREN")
    suspend fun getRootFileList(
        @Query(QUERY_SKIPTOKEN) skipToken: String? = null,
    ): Response<ListFolderResponse>

    @GET("$V1/$DRIVE/$ITEMS/{$FOLDER_ID}/$CHILDREN")
    suspend fun getFolderFileList(
        @Path(FOLDER_ID) folderId: String,
        @Query(QUERY_SKIPTOKEN) skipToken: String?,
    ): Response<ListFolderResponse>

    @POST("$V1/$DRIVE/$ROOT/$CHILDREN")
    suspend fun createFolderFromRoot(
        @Body createFolderRequest: CreateFolderRequestBody,
    ): Response<DriveItem>

    @POST("$V1/$DRIVE/$ITEMS/{$FOLDER_ID}/$CHILDREN")
    suspend fun createFolder(
        @Path(FOLDER_ID) folderId: String,
        @Body createFolderRequest: CreateFolderRequestBody,
    ): Response<DriveItem>

    @DELETE("$V1/$DRIVE/$ITEMS/{$ITEM_ID}")
    suspend fun deleteFile(
        @Path(ITEM_ID) itemId: String,
    ): Response<Unit>

    @PUT("$V1/$DRIVE/$ROOT:{$PATH}:/$CONTENT")
    suspend fun uploadFile(
        @Path(value = PATH, encoded = true) path: String,
        @Body filePart: RequestBody,
    ): Response<DriveItem>

    // Update file content by item id (small upload)
    @PUT("$V1/$DRIVE/$ITEMS/{$ITEM_ID}/$CONTENT")
    suspend fun updateFileContent(
        @Path(ITEM_ID) itemId: String,
        @Body filePart: RequestBody,
    ): Response<DriveItem>

    @POST("$V1/$DRIVE/$ROOT:{$PATH}:/$CREATE_UPLOAD_SESSION")
    suspend fun createUploadSession(
        @Path(value = PATH, encoded = true) path: String,
        @Header(CONTENT_TYPE) contentType: String? = "application/octet-stream",
    ): Response<UploadSessionResponse>

    // Create upload session for existing item (update large file)
    @POST("$V1/$DRIVE/$ITEMS/{$ITEM_ID}/$CREATE_UPLOAD_SESSION")
    suspend fun createUploadSessionForItem(
        @Path(ITEM_ID) itemId: String,
        @Header(CONTENT_TYPE) contentType: String? = "application/octet-stream",
    ): Response<UploadSessionResponse>

    // Search within root
    @GET("$V1/$DRIVE/$ROOT/search(q='{$Q}')")
    suspend fun searchInRoot(
        @Path(Q, encoded = true) query: String,
        @Query(QUERY_SKIPTOKEN) skipToken: String? = null,
    ): Response<ListFolderResponse>

    // Versions
    @GET("$V1/$DRIVE/$ITEMS/{$ITEM_ID}/$VERSIONS")
    suspend fun getItemVersions(
        @Path(ITEM_ID) itemId: String,
    ): Response<FileVersionsListResponse>

    @GET("$V1/$DRIVE/$ITEMS/{$ITEM_ID}/$VERSIONS/{$VERSION_ID}/$CONTENT")
    suspend fun downloadVersionContent(
        @Path(ITEM_ID) itemId: String,
        @Path(VERSION_ID) versionId: String,
    ): Response<ResponseBody>

    // Permissions
    @GET("$V1/$DRIVE/$ITEMS/{$ITEM_ID}/$PERMISSIONS")
    suspend fun getItemPermissions(
        @Path(ITEM_ID) itemId: String,
    ): Response<PermissionsListResponse>

    @POST("$V1/$DRIVE/$ITEMS/{$ITEM_ID}/$INVITE")
    suspend fun createPermission(
        @Path(ITEM_ID) itemId: String,
        @Body body: CreatePermissionRequestBody,
    ): Response<PermissionsListResponse>

    @PATCH("$V1/$DRIVE/$ITEMS/{$ITEM_ID}/$PERMISSIONS/{$PERMISSION_ID}")
    suspend fun updatePermission(
        @Path(ITEM_ID) itemId: String,
        @Path(PERMISSION_ID) permissionId: String,
        @Body body: UpdatePermissionRequestBody,
    ): Response<PermissionResponse>

    @DELETE("$V1/$DRIVE/$ITEMS/{$ITEM_ID}/$PERMISSIONS/{$PERMISSION_ID}")
    suspend fun deletePermission(
        @Path(ITEM_ID) itemId: String,
        @Path(PERMISSION_ID) permissionId: String,
    ): Response<Unit>
}
