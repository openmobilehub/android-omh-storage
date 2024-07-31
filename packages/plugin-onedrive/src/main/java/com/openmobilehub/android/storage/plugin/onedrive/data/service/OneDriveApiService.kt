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

package com.openmobilehub.android.storage.plugin.onedrive.data.service

import androidx.annotation.VisibleForTesting
import com.microsoft.graph.drives.item.items.item.createuploadsession.CreateUploadSessionPostRequestBody
import com.microsoft.graph.drives.item.items.item.invite.InvitePostRequestBody
import com.microsoft.graph.drives.item.items.item.searchwithq.SearchWithQGetResponse
import com.microsoft.graph.models.DriveItem
import com.microsoft.graph.models.DriveItemUploadableProperties
import com.microsoft.graph.models.DriveItemVersionCollectionResponse
import com.microsoft.graph.models.Permission
import com.microsoft.kiota.ApiException
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.utils.toInputStream
import com.openmobilehub.android.storage.plugin.onedrive.OneDriveConstants
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import javax.net.ssl.HttpsURLConnection.HTTP_UNAUTHORIZED

@Suppress("TooManyFunctions")
internal class OneDriveApiService(private val apiClient: OneDriveApiClient) {
    private val driveIdCache = DriveIdCache(apiClient)
    internal val driveId get() = driveIdCache.driveId

    fun getFilesList(parentId: String): List<DriveItem> {
        return apiClient.graphServiceClient.drives().byDriveId(driveId).items()
            .byDriveItemId(parentId).children().get().value
    }

    fun uploadFile(file: File, path: String, conflictBehavior: String = "rename"): DriveItem? {
        val fileStream = file.toInputStream()

        val uploadSessionRequest = CreateUploadSessionPostRequestBody().apply {
            item = DriveItemUploadableProperties().apply {
                additionalData["@microsoft.graph.conflictBehavior"] =
                    conflictBehavior
            }
        }

        val uploadSession = apiClient.graphServiceClient.drives()
            .byDriveId(driveId)
            .items()
            .byDriveItemId(path)
            .createUploadSession()
            .post(uploadSessionRequest)

        val uploadResult = apiClient.uploadFile(
            uploadSession,
            fileStream,
            file.length()
        )

        return if (uploadResult.isUploadSuccessful) uploadResult.itemResponse else null
    }

    fun downloadFile(fileId: String): InputStream? {
        return apiClient.graphServiceClient.drives()
            .byDriveId(driveId)
            .items()
            .byDriveItemId(fileId)
            .content()
            .get()
    }

    fun getFileVersions(fileId: String): DriveItemVersionCollectionResponse {
        return apiClient.graphServiceClient.drives().byDriveId(driveId).items()
            .byDriveItemId(fileId).versions().get()
    }

    fun downloadFileVersion(fileId: String, versionId: String): InputStream? {
        return apiClient.graphServiceClient.drives()
            .byDriveId(driveId)
            .items()
            .byDriveItemId(fileId)
            .versions()
            .byDriveItemVersionId(versionId)
            .content()
            .get()
    }

    fun search(query: String): SearchWithQGetResponse {
        return apiClient.graphServiceClient.drives().byDriveId(driveId).items()
            .byDriveItemId(OneDriveConstants.ROOT_FOLDER).searchWithQ(query)
            .get()
    }

    fun deleteFile(fileId: String) {
        apiClient.graphServiceClient.drives()
            .byDriveId(driveId)
            .items()
            .byDriveItemId(fileId).delete()
    }

    fun getFile(fileId: String): DriveItem? {
        return apiClient.graphServiceClient.drives()
            .byDriveId(driveId)
            .items()
            .byDriveItemId(fileId).get()
    }

    fun getFilePermissions(fileId: String): List<Permission> {
        return apiClient.graphServiceClient.drives()
            .byDriveId(driveId)
            .items()
            .byDriveItemId(fileId)
            .permissions()
            .get()
            .value
    }

    fun createPermission(fileId: String, body: InvitePostRequestBody): List<Permission> {
        return apiClient.graphServiceClient.drives()
            .byDriveId(driveId)
            .items()
            .byDriveItemId(fileId)
            .invite()
            .post(body)
            .value
    }

    fun deletePermission(fileId: String, permissionId: String) {
        apiClient.graphServiceClient.drives()
            .byDriveId(driveId)
            .items()
            .byDriveItemId(fileId)
            .permissions()
            .byPermissionId(permissionId)
            .delete()
    }

    fun updatePermission(fileId: String, permissionId: String, role: String): Permission {
        return apiClient.graphServiceClient.drives()
            .byDriveId(driveId)
            .items()
            .byDriveItemId(fileId)
            .permissions()
            .byPermissionId(permissionId)
            .patch(
                Permission().apply {
                    this.roles = listOf(role)
                }
            )
    }

    fun createNewFile(
        fileName: String,
        parentId: String,
        inputStream: FileInputStream
    ): DriveItem? {
        return apiClient.graphServiceClient.drives()
            .byDriveId(driveId)
            .items()
            .byDriveItemId(parentId)
            .children()
            .byDriveItemId1(fileName)
            .content()
            .put(inputStream)
    }

    fun updateFileMetadata(fileId: String, driveItem: DriveItem): DriveItem {
        return apiClient.graphServiceClient.drives()
            .byDriveId(driveId)
            .items()
            .byDriveItemId(fileId)
            .patch(driveItem)
    }

    @VisibleForTesting
    internal class DriveIdCache(private val apiClient: OneDriveApiClient) {
        private var cachedDriveId: String? = null
        private var previousAccessToken: String? = null

        val driveId: String
            get() {
                val accessToken = apiClient.authProvider.accessToken
                cachedDriveId.let { driveId ->
                    if (driveId == null || previousAccessToken != accessToken) {
                        return retrieveDriveId().also {
                            previousAccessToken = accessToken
                            cachedDriveId = it
                        }
                    }
                    return driveId
                }
            }

        @VisibleForTesting
        internal fun retrieveDriveId(): String {
            try {
                return apiClient.graphServiceClient.me().drive().get().id
            } catch (exception: ApiException) {
                if (exception.responseStatusCode == HTTP_UNAUTHORIZED) {
                    throw exception
                }
                throw OmhStorageException.ApiException(
                    message = "Couldn't get drive id",
                    cause = exception
                )
            }
        }
    }
}
